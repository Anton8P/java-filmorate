package ru.yandex.practicum.filmorate.storage.friendship;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Friendship;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class InMemoryFriendshipStorage implements FriendshipStorage {

    private final Map<Long, Friendship> friendshipStorage = new HashMap<>();

    @Override
    public Friendship save(Friendship friendship) {
        if (friendship.getId() == null) {
            long id = getNextId();
            friendship.setId(id);
        }
        friendshipStorage.put(friendship.getId(), friendship);
        return friendship;
    }

    @Override
    public Optional<Friendship> findByUsers(Long user1, Long user2) {
        return friendshipStorage.values().stream()
                .filter(friendship -> isSameFriendship(friendship, user1, user2))
                .findFirst();
    }

    @Override
    public List<Friendship> findByUserId(Long userId) {
        return friendshipStorage.values().stream()
                .filter(friendship -> friendship.getUserId().equals(userId) ||
                        friendship.getFriendId().equals(userId))
                .collect(Collectors.toList());
    }

    private boolean isSameFriendship(Friendship friendship, Long user1, Long user2) {
        return (friendship.getUserId().equals(user1) && friendship.getFriendId().equals(user2)) ||
                (friendship.getUserId().equals(user2) && friendship.getFriendId().equals(user1));
    }

    private long getNextId() {
        long currentMaxId = friendshipStorage.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0L);
        return ++currentMaxId;
    }
}
