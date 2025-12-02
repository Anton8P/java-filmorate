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
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.CreateGroup;
import ru.yandex.practicum.filmorate.validation.UpdateGroup;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/users")
@Validated
@Slf4j
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User add(@Validated(CreateGroup.class) @RequestBody User user) {
        log.info("POST /users - добавление пользователя: {}", user);
        boolean emailExists = users.values().stream()
                .anyMatch(existingUser -> user.getEmail().equals(existingUser.getEmail()));
        if (emailExists) {
            log.warn("POST /users - email уже используется: '{}'", user.getEmail());
            throw new ValidationException("Этот email уже используется");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("POST /users - имя не указано, установлен логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("POST /users - пользователь успешно добавлен. ID: {}, имя: {}",
                user.getId(), user.getName());
        return user;
    }

    @PutMapping
    public User update(@Validated(UpdateGroup.class) @RequestBody User user) {
        log.info("PUT /users - обновление пользователя: {}", user);
        User existingUser = users.get(user.getId());
        if (existingUser == null) {
            log.warn("PUT /users - пользователь с ID {} не найден", user.getId());
            throw new ValidationException("Пользователь с id " + user.getId() + " не найден");
        }
        existingUser.setLogin(user.getLogin());
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
        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }
        existingUser.setBirthday(user.getBirthday());
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
