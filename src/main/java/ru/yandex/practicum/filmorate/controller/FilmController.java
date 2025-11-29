package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film add(@Valid @RequestBody Film film) {
        log.info("POST /films - добавление фильма: {}", film);
        if (film == null) {
            log.warn("POST /films - передан null вместо фильма");
            throw new ValidationException("Null вместо фильма");
        }
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("POST /films - пустое название фильма");
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("POST /films - длина описания фильма превышена: {}", film.getDescription().length());
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        final LocalDate cinemaBirthday = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate() == null) {
            log.warn("POST /films - дата релиза не указана");
            throw new ValidationException("Дата релиза обязательна");
        }
        if (film.getReleaseDate().isBefore(cinemaBirthday)) {
            log.warn("POST /films - дата релиза раньше дня рождения кино: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.warn("POST /films - некорректная продолжительность фильма: {}",
                    film.getDuration() != null ? film.getDuration() + " минут" : "продолжительность не указана");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("POST /films - фильм успешно добавлен. ID: {}, название: {}", film.getId(), film.getName());
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("PUT /films - обновление фильма: {}", film);
        if (film == null) {
            log.warn("PUT /films - передан null вместо фильма");
            throw new ValidationException("Фильм не может быть null");
        }
        if (film.getId() == null) {
            log.warn("PUT /films - не указан ID фильма");
            throw new ValidationException("Должен быть указан ID фильма");
        }
        Film existingFilm = films.get(film.getId());
        if (existingFilm == null) {
            log.warn("PUT /films - фильм с ID {} не найден", film.getId());
            throw new ValidationException("Фильм с ID " + film.getId() + " не найден");
        }
        if (film.getName() != null) {
            if (film.getName().isBlank()) {
                log.warn("PUT /films - пустое название фильма");
                throw new ValidationException("Название не может быть пустым");
            }
            existingFilm.setName(film.getName());
        }
        if (film.getDescription() != null) {
            if (film.getDescription().length() > 200) {
                log.warn("PUT /films - некорректная длина описания {}", film.getDescription().length());
                throw new ValidationException("Максимальная длина описания — 200 символов");
            }
            existingFilm.setDescription(film.getDescription());
        }
        if (film.getReleaseDate() != null) {
            final LocalDate cinemaBirthday = LocalDate.of(1895, 12, 28);
            if (film.getReleaseDate().isBefore(cinemaBirthday)) {
                log.warn("PUT /films - дата релиза раньше дня рождения кино: {}", film.getReleaseDate());
                throw new ValidationException("Дата релиза фильма не может быть раньше 28 декабря 1895 года");
            }
            existingFilm.setReleaseDate(film.getReleaseDate());
        }
        if (film.getDuration() != null) {
            if (film.getDuration() <= 0) {
                log.warn("PUT /films - некорректная продолжительность фильма: {}",
                        film.getDuration() + " минут");
                throw new ValidationException("Продолжительность фильма должна быть положительным числом");
            }
            existingFilm.setDuration(film.getDuration());
        }
        log.info("PUT /films - фильм успешно обновлен. ID: {}, название: {}",
                existingFilm.getId(), existingFilm.getName());
        return existingFilm;
    }

    private int getNextId() {
        int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
