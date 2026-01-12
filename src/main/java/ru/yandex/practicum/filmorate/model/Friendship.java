package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.validation.CreateGroup;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Friendship {
    @JsonIgnore
    private Long id;
    @NotNull(message = "ID не должен быть null", groups = CreateGroup.class)
    private Long userId;
    @NotNull(message = "ID не должен быть null", groups = CreateGroup.class)
    private Long friendId;
    @JsonIgnore
    private FriendshipStatus status;
    @JsonIgnore
    private LocalDateTime created;

    public Friendship(Long userId, Long friendId) {
        this(null, userId, friendId, FriendshipStatus.PENDING, LocalDateTime.now());
    }

    public Friendship(Long id, Long userId, Long friendId, FriendshipStatus status, LocalDateTime created) {
        this.id = id;
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
        this.created = created;
    }

    public static Friendship createRequest(Long senderId, Long receiverId) {
        return new Friendship(null, senderId, receiverId,
                FriendshipStatus.PENDING, LocalDateTime.now());
    }

    public enum FriendshipStatus {
        PENDING,
        CONFIRMED
    }
}