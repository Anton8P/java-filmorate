package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FriendshipService {
    private final FriendshipStorage friendshipStorage;

    public FriendshipService(FriendshipStorage friendshipStorage) {
        this.friendshipStorage = friendshipStorage;
    }

    public Friendship sendRequestToFriendship(Long senderId, Long receiverId) {
        log.info("sendRequestToFriendship - отправка запроса от {} к {}", senderId, receiverId);
        if (friendshipStorage.findByUsers(senderId, receiverId).isPresent()) {
            log.info("sendRequestToFriendship - запрос от {} к {} уже существует", senderId, receiverId);
            throw new ValidationException("Запрос уже существует");
        }
        Friendship request = new Friendship(senderId, receiverId);
        log.info("sendRequestToFriendship - запрос от {} к {} успешно сохранен", senderId, receiverId);
        return friendshipStorage.save(request);
    }

    public Friendship confirmRequest(Long confirmingUserId, Long otherUserId) {
        log.info("confirmRequest - подтверждение запроса: пользователь {} подтверждает дружбу с {}",
                confirmingUserId, otherUserId);
        Friendship friendship = friendshipStorage.findByUsers(confirmingUserId, otherUserId)
                .orElseThrow(() -> {
                    log.info("confirmRequest - запрос не найден: {} и {}", confirmingUserId, otherUserId);
                    return new NotFoundException("Запрос не найден");
                });
        if (friendship.getStatus() != Friendship.FriendshipStatus.PENDING) {
            log.info("confirmRequest - запрос от {} к {} уже обработан", otherUserId, confirmingUserId);
            throw new ValidationException("Запрос уже обработан");
        }
        if (!friendship.getFriendId().equals(confirmingUserId)) {
            log.info("confirmRequest - подтвердить запрос может только получатель запроса: {}", confirmingUserId);
            throw new ValidationException(
                    "Подтвердить дружбу может только получатель запроса. " +
                            "Отправитель: " + friendship.getUserId() + ", " +
                            "Получатель: " + friendship.getFriendId()
            );
        }
        friendship.setStatus(Friendship.FriendshipStatus.CONFIRMED);
        log.info("confirmRequest - запрос от {} к {} подтвержден", otherUserId, confirmingUserId);
        return friendshipStorage.save(friendship);
    }

    public List<Long> getFriends(Long userId) {
        log.info("getFriends - получение списка друзей пользователя {}", userId);
        return friendshipStorage.findByUserId(userId).stream()
                .filter(friendship -> friendship.getStatus() == Friendship.FriendshipStatus.CONFIRMED)
                .map(friendship -> friendship.getUserId().equals(userId) ?
                        friendship.getFriendId() : friendship.getUserId())
                .collect(Collectors.toList());
    }
}
