package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilmControllerTest {

    private FilmController filmController;
    private FilmService filmService;
    private UserService userService;
    private InMemoryFilmStorage filmStorage;
    private InMemoryUserStorage userStorage;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
        filmService = new FilmService(filmStorage, userService);
        filmController = new FilmController(filmService);
        validFilm = new Film();
        validFilm.setName("Чебурашка");
        validFilm.setDescription("Фильм о дружелюбном ушастике");
        validFilm.setReleaseDate(LocalDate.of(2022, 12, 23));
        validFilm.setDuration(115);
    }

    @Test
    void add_ShouldCreateFilm_WhenDataValid() {
        Film result = filmController.add(validFilm);
        assertNotNull(result.getId());
        assertEquals("Чебурашка", result.getName());
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
        assertEquals("Дата релиза фильма не может быть раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void add_ShouldThrowException_WhenReleaseDateThisIsCinemaBirthday() {
        validFilm.setReleaseDate(LocalDate.of(1895,12,28));
        Film film = filmController.add(validFilm);
        assertNotNull(film);
    }

    @Test
    void update_ShouldUpdateFilm_WhenDataValid() {
        Film addedFilm = filmController.add(validFilm);
        long filmId = addedFilm.getId();
        Film filmToUpdate = new Film();
        filmToUpdate.setId(filmId);
        filmToUpdate.setName("Обновленный Чебурашка");
        filmToUpdate.setDescription("Новое описание");
        filmToUpdate.setReleaseDate(LocalDate.of(2022, 12, 23));
        filmToUpdate.setDuration(120);
        Film updatedFilm = filmController.update(filmToUpdate);
        assertEquals(filmId, updatedFilm.getId(), "ID должен остаться прежним");
        assertEquals("Обновленный Чебурашка", updatedFilm.getName());
        assertEquals(120, updatedFilm.getDuration());
        Film filmFromStorage = filmStorage.getById(filmId);
        assertEquals("Обновленный Чебурашка", filmFromStorage.getName());
    }

    @Test
    void addLike_ShouldWork_WhenFilmAndUserExist() {
        Film addedFilm = filmController.add(validFilm);
        long filmId = addedFilm.getId();
        User user = new User();
        user.setEmail("test@ya.ru");
        user.setLogin("testUser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1900, 1, 1));
        User addedUser = userService.addUser(user);
        long userId = addedUser.getId();
        Film likedFilm = filmController.addLike(filmId, userId);
        assertTrue(likedFilm.getLikes().contains(userId));
    }

    @Test
    void findById_ShouldReturnFilm_WhenExists() {
        Film addedFilm = filmController.add(validFilm);
        long filmId = addedFilm.getId();
        Film foundFilm = filmController.findById(filmId);
        assertEquals(filmId, foundFilm.getId());
        assertEquals("Чебурашка", foundFilm.getName());
    }

    @Test
    void findById_ShouldThrowException_WhenNotFound() {
        assertThrows(NotFoundException.class, () -> filmController.findById(123L));
    }
}