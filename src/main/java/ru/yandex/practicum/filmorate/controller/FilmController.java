package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.validation.CreateGroup;
import ru.yandex.practicum.filmorate.validation.UpdateGroup;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
@Validated
@Slf4j
public class FilmController {
    private final FilmService filmService;
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(
            1895, 12, 28);

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("GET /films");
        return filmService.findAllFilms();
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable("id") Long id) {
        log.info("GET /films/{}", id);
        return filmService.getFilmById(id);
    }

    @PostMapping
    public Film add(@Validated(CreateGroup.class) @RequestBody Film film) {
        log.info("POST /films - добавление фильма: {}", film.getName());
        validateReleaseDate(film.getReleaseDate());
        Film savedFilm = filmService.addFilm(film);
        log.info("POST /films - запрос обработан успешно");
        return savedFilm;
    }

    @PutMapping
    public Film update(@Validated(UpdateGroup.class) @RequestBody Film film) {
        log.info("PUT /films - обновление фильма ID: {}, название: {}",
                film.getId(), film.getName());
        validateReleaseDate(film.getReleaseDate());
        Film updatedFilm = filmService.updateFilm(film);
        log.info("PUT /films - фильм успешно обновлен");
        return updatedFilm;
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable("id") Long filmId, @PathVariable("userId") Long userId) {
        log.info("PUT /films/{}/like/{}", filmId, userId);
        return filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLike(@PathVariable("id") Long filmId, @PathVariable("userId") Long userId) {
        log.info("DELETE /films/{}/like/{}", filmId, userId);
        return filmService.removeLike(filmId, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(name = "count", required = false,
            defaultValue = "10") Integer count) {
        log.info("GET /films/popular?count={}", count);
        return filmService.getPopularFilms(count);
    }

    private void validateReleaseDate(LocalDate releaseDate) {
        if (releaseDate != null && releaseDate.isBefore(CINEMA_BIRTHDAY)) {
            throw new ValidationException("Дата релиза фильма не может быть раньше 28 декабря 1895 года");
        }
    }

}