package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserControllerTest {

    private UserController userController;
    private User validUser;

    @BeforeEach
    void setUp() {
        userController = new UserController();
        validUser = new User();
        validUser.setLogin("validLogin");
        validUser.setEmail("test@yandex.ru");
        validUser.setBirthday(LocalDate.of(1986,8,20));
    }

    private User addUser() {
        return userController.add(validUser);
    }

    @Test
    void add_ShouldThrowException_WhenLoginContainsSpaces() {
        validUser.setLogin("login and spaces");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.add(validUser));
        assertEquals("Логин не может содержать пробелы", exception.getMessage());
    }

    @Test
    void add_ShouldThrowException_WhenLoginIsBlank() {
        validUser.setLogin(" ");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.add(validUser));
        assertEquals("Логин не может быть пустым", exception.getMessage());
    }

    @Test
    void add_ShouldThrowException_WhenEmailWithoutDogSymbol() {
        validUser.setEmail("invalid-email");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.add(validUser));
        assertEquals("Электронная почта должна содержать символ @", exception.getMessage());
    }

    @Test
    void add_ShouldThrowException_WhenBirthdayInFuture() {
        validUser.setBirthday(LocalDate.now().plusDays(1));
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.add(validUser));
        assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
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
        updateUser.setId(123);
        updateUser.setLogin("newLogin");
        updateUser.setEmail("newEmail@ya.ru");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.update(updateUser));
        assertEquals("Пользователь с id 123 не найден", exception.getMessage());
    }

    @Test
    void update_ShouldThrowException_WhenIdIsNull() {
        User updateUser = new User();
        updateUser.setLogin("newLogin");
        updateUser.setEmail("newEmail@ya.ru");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.update(updateUser));
        assertEquals("Должен быть указан ID пользователя", exception.getMessage());
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
        assertEquals("Этот email уже используется", exception.getMessage());
    }

    @Test
    void update_ShouldAllowSameEmail_WhenEmailNotChanged() {
        User addedUser = addUser();
        User updateUser = new User();
        updateUser.setId(addedUser.getId());
        updateUser.setLogin("updatedLogin");
        User result = userController.update(updateUser);
        assertEquals("updatedLogin", result.getLogin());
        assertEquals("test@yandex.ru", result.getEmail());
    }
}