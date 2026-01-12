package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
        long newId = getNextId();
        user.setId(newId);
        users.put(newId, user);
        return user;
    }

    @Override
    public User update(User user) {
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
