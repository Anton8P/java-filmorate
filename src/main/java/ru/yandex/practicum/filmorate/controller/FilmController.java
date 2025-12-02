package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.CreateGroup;
import ru.yandex.practicum.filmorate.validation.UpdateGroup;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Validated
@Slf4j
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film add(@Validated(CreateGroup.class) @RequestBody Film film) {
        log.info("POST /films - добавление фильма: {}", film);
        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("POST /films - дата релиза раньше дня рождения кино: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("POST /films - фильм успешно добавлен. ID: {}, название: {}", film.getId(), film.getName());
        return film;
    }

    @PutMapping
    public Film update(@Validated(UpdateGroup.class) @RequestBody Film film) {
        log.info("PUT /films - обновление фильма: {}", film);
        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("PUT /films - дата релиза раньше дня рождения кино: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза фильма не может быть раньше 28 декабря 1895 года");
        }
        Film existingFilm = films.get(film.getId());
        if (existingFilm == null) {
            log.warn("PUT /films - фильм с ID {} не найден", film.getId());
            throw new ValidationException("Фильм с ID " + film.getId() + " не найден");
        }
        existingFilm.setName(film.getName());
        existingFilm.setDescription(film.getDescription());
        existingFilm.setReleaseDate(film.getReleaseDate());
        existingFilm.setDuration(film.getDuration());
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
