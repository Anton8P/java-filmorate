package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.CreateGroup;
import ru.yandex.practicum.filmorate.validation.UpdateGroup;

import java.time.LocalDate;

@Data
public class User {
    @Null(message = "ID должен быть null при создании", groups = CreateGroup.class)
    @NotNull(message = "ID обязателен при обновлении", groups = UpdateGroup.class)
    private Integer id;

    @NotBlank(message = "Email не может быть пустым", groups = CreateGroup.class)
    @Email(message = "Email должен быть в правильном формате", groups = {CreateGroup.class, UpdateGroup.class})
    private String email;

    @NotBlank(message = "Логин не может быть пустым", groups = {CreateGroup.class, UpdateGroup.class})
    @Pattern(regexp = "\\S+", message = "Логин не может содержать пробелы", groups = {CreateGroup.class, UpdateGroup.class})
    private String login;

    private String name;

    @NotNull(message = "Дата рождения обязательна", groups = {CreateGroup.class, UpdateGroup.class})
    @PastOrPresent(message = "Дата рождения не может быть в будущем", groups = {CreateGroup.class, UpdateGroup.class})
    private LocalDate birthday;
}
