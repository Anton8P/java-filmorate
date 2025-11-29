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
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User add(@Valid @RequestBody User user) {
        log.info("POST /users - добавление пользователя: {}", user);
        if (user == null) {
            log.warn("POST /users - передан null вместо пользователя");
            throw new ValidationException("Null вместо пользователя");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("POST /users - логин пустой или null: '{}'", user.getLogin());
            throw new ValidationException("Логин не может быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            log.warn("POST /users - логин содержит пробелы: '{}'", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("POST /users - email пустой или null: '{}'", user.getEmail());
            throw new ValidationException("Электронная почта не может быть пустой");
        }
        if (!user.getEmail().contains("@")) {
            log.warn("POST /users - email не содержит @: '{}'", user.getEmail());
            throw new ValidationException("Электронная почта должна содержать символ @");
        }
        for (User oldUser : users.values()) {
            if (oldUser.getEmail() != null && oldUser.getEmail().equals(user.getEmail())) {
                log.warn("POST /users - email уже используется: '{}'", user.getEmail());
                throw new ValidationException("Этот имейл уже используется");
            }
        }
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("POST /users - имя не указано, установлен логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
        if (user.getBirthday() == null) {
            log.warn("POST /users - дата рождения не указана");
            throw new ValidationException("Дата рождения обязательна");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("POST /users - дата рождения указана в будущем: {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("POST /users - пользователь успешно добавлен. ID: {}, имя: {}",
                user.getId(), user.getName());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.info("PUT /users - обновление пользователя: {}", user);
        if (user == null) {
            log.warn("PUT /users - передан null вместо пользователя");
            throw new ValidationException("Пользователь не может быть null");
        }
        if (user.getId() == null) {
            log.warn("PUT /users - не указан ID пользователя");
            throw new ValidationException("Должен быть указан ID пользователя");
        }
        User existingUser = users.get(user.getId());
        if (existingUser == null) {
            log.warn("PUT /users - пользователь с ID {} не найден", user.getId());
            throw new ValidationException("Пользователь с id " + user.getId() + " не найден");
        }
        if (user.getLogin() != null) {
            if (user.getLogin().isBlank()) {
                log.warn("PUT /users - пустое поле логина");
                throw new ValidationException("Логин не может быть пустым");
            }
            if (user.getLogin().contains(" ")) {
                log.warn("PUT /users - поле логина содержит пробелы");
                throw new ValidationException("Логин не может содержать пробелы");
            }
            existingUser.setLogin(user.getLogin());
        }
        if (user.getEmail() != null) {
            if (user.getEmail().isBlank()) {
                log.warn("PUT /users - электронная почта не задана");
                throw new ValidationException("Электронная почта не может быть пустой");
            }

            if (!user.getEmail().contains("@")) {
                log.warn("PUT /users - электронная почта не содержит символ @");
                throw new ValidationException("Электронная почта должна содержать символ @");
            }
            boolean emailChanged = !Objects.equals(user.getEmail(), existingUser.getEmail());
            if (emailChanged) {
                boolean emailAlreadyUsed = users.values().stream()
                        .filter(otherUser -> !otherUser.getId().equals(user.getId()))
                        .anyMatch(otherUser -> Objects.equals(user.getEmail(), otherUser.getEmail()));
                if (emailAlreadyUsed) {
                    log.warn("PUT /users - email '{}' уже используется другим пользователем", user.getEmail());
                    throw new ValidationException("Этот email уже используется");
                }
                existingUser.setEmail(user.getEmail());
            }
        }
        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }
        if (user.getBirthday() != null) {
            if (user.getBirthday().isAfter(LocalDate.now())) {
                log.warn("PUT /users - дата рождения позже текущего времени");
                throw new ValidationException("Дата рождения не может быть в будущем");
            }
            existingUser.setBirthday(user.getBirthday());
        }
        log.info("PUT /users - пользователь успешно обновлен. ID: {}, имя: {}",
                existingUser.getId(), existingUser.getName());
        return existingUser;
    }

    private int getNextId() {
        int currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
