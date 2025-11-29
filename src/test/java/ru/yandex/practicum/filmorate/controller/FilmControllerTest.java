package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {

    private FilmController filmController;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
        validFilm = new Film();
        validFilm.setName("Чебурашка");
        validFilm.setDescription("Фильм о дружелюбном ушастике");
        validFilm.setReleaseDate(LocalDate.of(2022,12,23));
        validFilm.setDuration(115);
    }

    @Test
    void add_ShouldCreateFilm_WhenDataValid() {
        Film result = filmController.add(validFilm);
        assertNotNull(result.getId());
        assertEquals("Чебурашка", result.getName());
    }

    @Test
    void add_ShouldThrowException_WhenNameIsBlank() {
        validFilm.setName(" ");
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.add(validFilm));
        assertEquals("Название не может быть пустым", exception.getMessage());
    }

    @Test
    void add_ShouldThrowException_WhenNameIsNull() {
        validFilm.setName(null);
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.add(validFilm));
        assertEquals("Название не может быть пустым", exception.getMessage());
    }

    @Test
    void add_ShouldThrowException_WhenDescriptionTooLong() {
        validFilm.setDescription("D".repeat(201));
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.add(validFilm));
        assertEquals("Максимальная длина описания — 200 символов", exception.getMessage());
    }

    @Test
    void add_ShouldAccept_WhenDescriptionLess200Char() {
        validFilm.setDescription("D".repeat(200));
        assertEquals(200, validFilm.getDescription().length());
    }

    @Test
    void add_ShouldThrowException_WhenReleaseDateBeforeCinemaBirthday() {
        validFilm.setReleaseDate(LocalDate.of(1895,12,27));
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.add(validFilm));
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void add_ShouldThrowException_WhenReleaseDateThisIsCinemaBirthday() {
        validFilm.setReleaseDate(LocalDate.of(1895,12,28));
        Film film = filmController.add(validFilm);
        assertNotNull(film);
    }

    @Test
    void add_ShouldThrowException_WhenDurationFilmIsZero() {
        validFilm.setDuration(0);
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.add(validFilm));
        assertEquals("Продолжительность фильма должна быть положительным числом", exception.getMessage());
    }

    @Test
    void add_ShouldThrowException_WhenDurationFilmIsNegative() {
        validFilm.setDuration(-1);
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.add(validFilm));
        assertEquals("Продолжительность фильма должна быть положительным числом", exception.getMessage());
    }

    @Test
    void add_ShouldThrowException_WhenDurationFilmIsNull() {
        validFilm.setDuration(null);
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.add(validFilm));
        assertEquals("Продолжительность фильма должна быть положительным числом", exception.getMessage());
    }

    @Test
    void update_ShouldUpdateFilm_WhenDataValid() {
        Film addedFilm = filmController.add(validFilm);
        addedFilm.setName("Добавленный фильм");
        Film updatedFilm = filmController.add(validFilm);
        updatedFilm.setId(addedFilm.getId());
        updatedFilm.setName("Новый фильм");
        Film result = filmController.add(updatedFilm);
        assertEquals("Новый фильм", result.getName());
    }

    @Test
    void update_ShouldThrowException_WhenFilmNotFound() {
        Film nonExistentFilm = new Film();
        nonExistentFilm.setId(123);
        nonExistentFilm.setName("Несуществующий фильм");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.update(nonExistentFilm));
        assertEquals("Фильм с ID 123 не найден", exception.getMessage());
    }
}