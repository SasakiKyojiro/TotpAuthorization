package ru.istokmw.testotp.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties.Jwt;


@Getter
@ToString
@Builder
public class ValidateResponse {
    private Jwt token;
    private Boolean f2pa;
    private Boolean success;
    private String message;
}
