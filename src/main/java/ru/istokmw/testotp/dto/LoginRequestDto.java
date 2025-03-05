package ru.istokmw.testotp.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record LoginRequestDto(
        @JsonProperty("email")
        @Email(message = "Введите действующую электронную почту")
        @Size(min = 2, max = 50, message = "Не меньше 2 и не больше 50 знаков")
        @NotNull String email,
        @JsonProperty("password")
        @Size(min = 6, max = 50, message = "Не меньше 6 и не больше 50 знаков")
        @NotNull String password) {
}
