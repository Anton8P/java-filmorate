package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getById(Long id) {
        if (id <= 0 || !films.containsKey(id)) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }
        return films.get(id);
    }

    @Override
    public Film add(Film film) {
        if (film == null) {
            throw new IllegalArgumentException("Фильм не может быть null");
        }
        if (film.getId() != null) {
            throw new IllegalArgumentException("При создании фильма ID должен быть null");
        }
        long newId = getNextId();
        film.setId(newId);
        films.put(newId, film);
        log.debug("Фильм добавлен: ID: {}, название: {}", newId, film.getName());
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film == null) {
            throw new IllegalArgumentException("Фильм не может быть null");
        }
        if (film.getId() == null) {
            throw new IllegalArgumentException("ID не может быть null при обновлении");
        }
        films.put(film.getId(), film);
        return film;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0L);
        return ++currentMaxId;
    }
}
