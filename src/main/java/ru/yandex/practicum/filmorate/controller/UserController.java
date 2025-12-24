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
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.validation.CreateGroup;
import ru.yandex.practicum.filmorate.validation.UpdateGroup;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users")
@Validated
@Slf4j
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        log.info("GET /users");
        return userService.findAllUsers();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable("id") Long id) {
        log.info("GET /users/{}", id);
        return userService.getUserById(id);
    }

    @PostMapping
    public User add(@Validated(CreateGroup.class) @RequestBody User user) {
        log.info("POST /users - добавление пользователя: Имя: {}, Email: {}", user.getName(), user.getEmail());
        User savedUser = userService.addUser(user);
        log.info("POST /users - запрос обработан успешно");
        return savedUser;
    }

    @PutMapping
    public User update(@Validated(UpdateGroup.class) @RequestBody User user) {
        log.info("PUT /users - обновление пользователя: Имя: {}, Email: {}", user.getName(), user.getEmail());
        User updatedUser = userService.updateUser(user);
        log.info("PUT /users - пользователь успешно обновлен");
        return updatedUser;
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable("id") Long userId, @PathVariable("friendId") Long friendId) {
        log.info("PUT /users/{}/friends/{}", userId, friendId);
        return userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User deleteFriend(@PathVariable("id") Long userId, @PathVariable("friendId") Long friendId) {
        log.info("DELETE /users/{}/friends/{}", userId, friendId);
        return userService.removeFriend(userId, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable("id") Long userId) {
        log.info("GET /users/{}/friends", userId);
        return userService.getFriends(userId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable("id") Long userId1, @PathVariable("otherId") Long userId2) {
        log.info("GET /users/{}/friends/common/{}", userId1, userId2);
        return userService.getCommonFriends(userId1, userId2);
    }
}
