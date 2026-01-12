package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.service.FriendshipService;
import ru.yandex.practicum.filmorate.validation.CreateGroup;

@Slf4j
@RestController
@RequestMapping("/friendships")
@Validated
public class FriendshipController {
    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @PostMapping("/requests")
    public Friendship sendRequest(@Validated(CreateGroup.class) @RequestBody Friendship friendship) {
        log.info("POST /friendships - Отправка запроса: {} к {}",
                friendship.getUserId(), friendship.getFriendId());
        return friendshipService.sendRequestToFriendship(friendship.getUserId(), friendship.getFriendId());
    }

    @PutMapping("/confirm")
    public Friendship confirmRequest(@RequestParam("confirmingUserId")
                                     @Min(value = 1, message = "ID должен быть положительным") Long confirmingUserId,
                                     @RequestParam("otherUserId")
                                     @Min(value = 1, message = "ID должен быть положительным") Long otherUserId) {
        log.info("PUT /friendships - Подтверждение запроса: {} подтверждает с {}",
                confirmingUserId, otherUserId);
        return friendshipService.confirmRequest(confirmingUserId, otherUserId);
    }
}
