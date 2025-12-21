package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getById(Long id) {
        if (id <= 0 || !users.containsKey(id)) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
        return users.get(id);
    }

    @Override
    public User add(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не может быть null");
        }
        if (user.getId() != null) {
            throw new IllegalArgumentException("При создании Пользователя ID должен быть null");
        }
        long newId = getNextId();
        user.setId(newId);
        users.put(newId, user);
        log.debug("Пользователь добавлен: ID: {}, Имя: {}", newId, user.getName());
        return user;
    }

    @Override
    public User update(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не может быть null");
        }
        if (user.getId() == null) {
            throw new IllegalArgumentException("ID не может быть null при обновлении");
        }
        users.put(user.getId(), user);
        return user;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0L);
        return ++currentMaxId;
    }

}
