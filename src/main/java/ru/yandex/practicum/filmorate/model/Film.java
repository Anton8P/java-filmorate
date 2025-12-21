package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.CreateGroup;
import ru.yandex.practicum.filmorate.validation.UpdateGroup;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    @Null(message = "ID должен быть null при создании", groups = CreateGroup.class)
    @NotNull(message = "ID обязателен при обновлении", groups = UpdateGroup.class)
    private Long id;

    @NotBlank(message = "Название не может быть пустым", groups = {CreateGroup.class, UpdateGroup.class})
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов", groups = {CreateGroup.class, UpdateGroup.class})
    private String description;

    @NotNull(message = "Дата релиза обязательна", groups = {CreateGroup.class, UpdateGroup.class})
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer duration;

    private final Set<Long> likes = new HashSet<>();

    public Set<Long> getLikes() {
        return new HashSet<>(this.likes);
    }

    public void addLike(Long userId) {
        this.likes.add(userId);
    }

    public void removeLike(Long userId) {
        this.likes.remove(userId);
    }

    public boolean hasLike(Long userId) {
        return this.likes.contains(userId);
    }

    public int getLikesCount() {
        return this.likes.size();
    }
}
