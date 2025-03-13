package ru.istokmw.testotp.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class ValidateResponse {
    private Boolean f2pa;
    private Boolean success;
    private String message;
}
