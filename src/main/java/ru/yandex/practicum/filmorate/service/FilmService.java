package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class FilmService {
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(
            1895, 12, 28);
    private static final int DEFAULT_POPULAR_COUNT = 10;

    private final FilmStorage filmStorage;
    private final UserService userService;

    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Collection<Film> findAllFilms() {
        return filmStorage.findAll();
    }

    public Film getFilmById(Long id) {
        Film film = filmStorage.getById(id);
        if (film == null) {
            throw new NotFoundException("Фильм с ID: " + id + " не найден");
        }
        return film;
    }

    public Film addFilm(Film film) {
        log.info("addFilm - добавление фильма: {}", film.getName());
        validateFilm(film);
        Film savedFilm = filmStorage.add(film);
        log.info("addFilm - фильм успешно добавлен. ID: {}, название: {}",
                savedFilm.getId(), savedFilm.getName());
        return savedFilm;
    }

    public Film updateFilm(Film film) {
        log.info("updateFilm - обновление фильма: {}", film);
        validateFilm(film);
        Film existingFilm = getFilmById(film.getId());
        existingFilm.setName(film.getName());
        existingFilm.setDescription(film.getDescription());
        existingFilm.setReleaseDate(film.getReleaseDate());
        existingFilm.setDuration(film.getDuration());
        Film updatedFilm = filmStorage.update(existingFilm);
        log.info("updateFilm - фильм успешно обновлен. ID: {}, название: {}",
                updatedFilm.getId(), updatedFilm.getName());

        return updatedFilm;
    }

    public Film addLike(Long filmId, Long userId) {
        log.info("Добавление лайка к фильму {} от пользователя {}", filmId, userId);
        Film film = getFilmById(filmId);
        userService.getUserById(userId);
        if (film.hasLike(userId)) {
            log.warn("Пользователь {} уже ставил лайк фильму {}", userId, filmId);
            throw new ValidationException(
                    String.format("Пользователь %d уже поставил лайк фильму %d", userId, filmId)
            );
        }
        film.addLike(userId);
        filmStorage.update(film);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
        return film;
    }

    public Film removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка фильму {} от пользователя {}", filmId, userId);
        Film film = getFilmById(filmId);
        userService.getUserById(userId);
        if (!film.hasLike(userId)) {
            log.warn("Пользователь {} не ставил лайк фильму {}", userId, filmId);
            throw new ValidationException(
                    String.format("Пользователь %d не ставил лайк фильму %d", userId, filmId)
            );
        }
        film.removeLike(userId);
        filmStorage.update(film);
        log.info("Пользователь {} удалил лайк фильму {}", userId, filmId);
        return film;
    }

    public List<Film> getPopularFilms(Integer count) {
        log.info("Получение {} популярных фильмов", count);
        int filmsCount = (count == null || count <= 0) ? DEFAULT_POPULAR_COUNT : count;
        List<Film> popularFilms = findAllFilms().stream()
                .sorted(Comparator.comparingInt(Film::getLikesCount).reversed())
                .limit(filmsCount)
                .toList();
        log.info("Популярных фильмов: {}", popularFilms.size());
        return popularFilms;
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            throw new ValidationException(
                    "Дата релиза фильма не может быть раньше 28 декабря 1895 года"
            );
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException(
                    "Продолжительность фильма должна быть положительной"
            );
        }
    }
}
