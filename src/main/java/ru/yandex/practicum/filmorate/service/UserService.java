package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> findAllUsers() {
        return userStorage.findAll();
    }

    public User getUserById(Long id) {
        User user = userStorage.getById(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID: " + id + " не найден");
        }
        return user;
    }

    public User addUser(User user) {
        log.info("addUser - добавление пользователя: {}", user.getName());
        validateEmailForCreate(user);
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("addUser - имя не указано, установлен логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
        User savedUser = userStorage.add(user);
        log.info("addUser - пользователь успешно добавлен. ID: {}, Имя: {}", savedUser.getId(), savedUser.getName());
        return savedUser;
    }

    public User updateUser(User user) {
        log.info("updateUser - обновление пользователя: {}", user);
        User existingUser = getUserById(user.getId());
        validateEmailForUpdate(user.getEmail(), user.getId());
        existingUser.setLogin(user.getLogin());
        existingUser.setEmail(user.getEmail());
        if (user.getName() == null || user.getName().isBlank()) {
            existingUser.setName(user.getLogin());
        } else {
            existingUser.setName(user.getName());
        }
        existingUser.setBirthday(user.getBirthday());
        userStorage.update(existingUser);
        log.info("updateUser - пользователь успешно обновлен. ID: {}, Имя: {}",
                existingUser.getId(), existingUser.getName());
        return existingUser;
    }

    public User addFriend(Long userId, Long friendId) {
        log.info("Добавление друга {} в друзья пользователю {}", friendId, userId);
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        if (userId.equals(friendId)) {
            log.warn("Попытка добавить самого себя в друзья. Пользователь ID: {}", userId);
            throw new ValidationException(String.format("Пользователь не может добавить сам себя в друзья. ID: %d", userId));
        }
        if (user.hasFriend(friendId)) {
            log.warn("Пользователи {} и {} уже друзья", userId, friendId);
            throw new ValidationException(String.format("Пользователь %d уже является другом пользователя %d", friendId, userId));
        }
        user.addFriend(friendId);
        friend.addFriend(userId);
        userStorage.update(user);
        userStorage.update(friend);
        log.info("Пользователи {} и {} теперь друзья", userId, friendId);
        return user;
    }

    public User removeFriend(Long userId, Long friendId) {
        log.info("Удаление друга {} у пользователя {}", friendId, userId);
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        if (!user.hasFriend(friendId)) {
            log.info("Пользователь {} не является другом пользователя {}. Возвращаем пользователя без изменений.",
                    friendId, userId);
            return user;
        }
        user.removeFriend(friendId);
        friend.removeFriend(userId);
        userStorage.update(user);
        userStorage.update(friend);
        log.info("Пользователи {} и {} больше не являются друзьями", userId, friendId);
        return user;
    }

    public List<User> getFriends(Long userId) {
        log.info("Получение списка друзей пользователя {}", userId);
        User user = getUserById(userId);
        return user.getFriends().stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId1, Long userId2) {
        log.info("Получение списка общих друзей пользователей {} и {}", userId1, userId2);
        User user1 = getUserById(userId1);
        User user2 = getUserById(userId2);
        Set<Long> commonIds = new HashSet<>(user1.getFriends());
        commonIds.retainAll(user2.getFriends());
        return commonIds.stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    private void validateEmailForCreate(User user) {
        boolean emailUsedByOther = userStorage.findAll().stream()
                .anyMatch(existingUser -> existingUser.getEmail().equalsIgnoreCase(user.getEmail()));
        if (emailUsedByOther) {
            throw new ValidationException("Email " + user.getEmail() + " уже используется");
        }
    }

    private void validateEmailForUpdate(String email, Long userId) {
        boolean emailUsedByOther = userStorage.findAll().stream()
                .filter(existingUser -> existingUser.getId() != null && !existingUser.getId().equals(userId))
                .filter(existingUser -> existingUser.getEmail() != null)
                .anyMatch(existingUser -> existingUser.getEmail().equalsIgnoreCase(email));
        if (emailUsedByOther) {
            throw new ValidationException("Email " + email + " уже используется");
        }
    }
}
