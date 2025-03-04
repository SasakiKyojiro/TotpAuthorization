package ru.istokmw.testotp.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class LoginRequestDto {
    @NotBlank
    private final String emailId;

    @NotBlank
    private final String password;
}
