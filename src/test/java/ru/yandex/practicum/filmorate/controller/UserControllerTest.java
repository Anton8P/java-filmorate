package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserControllerTest {

    private UserController userController;
    private UserService userService;
    private UserStorage userStorage;
    private User validUser;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
        userController = new UserController(userService);
        validUser = new User();
        validUser.setLogin("validLogin");
        validUser.setEmail("test@yandex.ru");
        validUser.setBirthday(LocalDate.of(1986,8,20));
    }

    private User addUser() {
        return userController.add(validUser);
    }

    @Test
    void add_ShouldSetLoginAsName_WhenNameIsBlank() {
        validUser.setName(" ");
        User result = userController.add(validUser);
        assertEquals("validLogin", result.getName());
    }

    @Test
    void update_ShouldUpdateUser_WhenDataValid() {
        User addedUser = addUser();
        User updateUser = new User();
        updateUser.setId(addedUser.getId());
        updateUser.setLogin("newLogin");
        updateUser.setEmail("newEmail@ya.ru");
        updateUser.setName("Новое Имя");
        updateUser.setBirthday(LocalDate.of(1990,5,1));
        User result = userController.update(updateUser);
        assertEquals("newLogin", result.getLogin());
        assertEquals("newEmail@ya.ru", result.getEmail());
        assertEquals("Новое Имя", result.getName());
        assertEquals(LocalDate.of(1990,5,1), result.getBirthday());
    }

    @Test
    void update_ShouldThrowException_WhenUserNotFound() {
        User updateUser = new User();
        updateUser.setId(123L);
        updateUser.setLogin("newLogin");
        updateUser.setEmail("newEmail@ya.ru");
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userController.update(updateUser));
        assertEquals("Пользователь с ID 123 не найден", exception.getMessage());
    }

    @Test
    void update_ShouldThrowException_WhenEmailAlreadyUsed() {
        User firstUser = addUser();
        User secondUser = new User();
        secondUser.setLogin("secondUser");
        secondUser.setEmail("second@ya.ru");
        secondUser.setBirthday(LocalDate.of(1990,1,1));
        userController.add(secondUser);
        User updateUser = new User();
        updateUser.setId(firstUser.getId());
        updateUser.setEmail("second@ya.ru");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.update(updateUser));
        assertEquals("Email second@ya.ru уже используется", exception.getMessage());
    }

    @Test
    void addFriend_ShouldAddFriend_WhenUsersExist() {
        User user1 = new User();
        user1.setLogin("user1");
        user1.setEmail("user1@ya.ru");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1999, 1, 1));
        User savedUser1 = userController.add(user1);

        User user2 = new User();
        user2.setLogin("user2");
        user2.setEmail("user2@ya.ru");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(2000, 2, 2));
        User savedUser2 = userController.add(user2);

        User updatedUser = userController.addFriend(savedUser1.getId(), savedUser2.getId());
        assertTrue(updatedUser.getFriends().contains(savedUser2.getId()));
        User user2FromStorage = userController.findById(savedUser2.getId());
        assertTrue(user2FromStorage.getFriends().contains(savedUser1.getId()));
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        User user1 = new User();
        user1.setLogin("user1");
        user1.setEmail("user1@ya.ru");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1999, 1, 1));
        userController.add(user1);

        User user2 = new User();
        user2.setLogin("user2");
        user2.setEmail("user2@ya.ru");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(2000, 2, 2));
        userController.add(user2);

        User user3 = new User();
        user3.setLogin("user3");
        user3.setEmail("user3@ya.ru");
        user3.setName("User Three");
        user3.setBirthday(LocalDate.of(2001, 3, 3));
        userController.add(user3);

        List<User> allUsers = userController.findAll().stream().toList();
        assertEquals(3, allUsers.size());
    }

    @Test
    void findById_ShouldReturnUser_WhenExists() {
        User addedUser = addUser();
        User foundUser = userController.findById(addedUser.getId());
        assertEquals(addedUser.getId(), foundUser.getId());
        assertEquals("validLogin", foundUser.getLogin());
    }
}