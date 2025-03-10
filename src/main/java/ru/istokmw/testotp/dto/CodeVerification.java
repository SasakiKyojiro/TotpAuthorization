package ru.istokmw.testotp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CodeVerification(
        @JsonProperty("email")
        @Email
        @NotNull String email,
        @Size(min = 8, max = 8, message = "Код должен состоять из 8 цифр")
        @NotNull String code
) {
}
