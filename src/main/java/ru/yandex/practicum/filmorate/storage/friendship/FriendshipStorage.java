package ru.yandex.practicum.filmorate.storage.friendship;

import ru.yandex.practicum.filmorate.model.Friendship;

import java.util.List;
import java.util.Optional;

public interface FriendshipStorage {
    Friendship save(Friendship friendship);

    Optional<Friendship> findByUsers(Long user1, Long user2);

    List<Friendship> findByUserId(Long userId);
}
