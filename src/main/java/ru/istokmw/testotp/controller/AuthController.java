package ru.istokmw.testotp.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.istokmw.testotp.dto.CodeVerification;
import ru.istokmw.testotp.dto.EmailDto;
import ru.istokmw.testotp.dto.LoginRequestDto;
import ru.istokmw.testotp.dto.ValidateResponse;
import ru.istokmw.testotp.service.AuthService;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<String>> register(@RequestBody @Validated LoginRequestDto loginRequestDto) {
        return authService.register(loginRequestDto)
                .map(isRegistered -> {
                    if (isRegistered) {
                        return ResponseEntity.ok("Регистрация прошла успешно!");
                    } else {
                        return ResponseEntity.badRequest()
                                .body("Регистрация не удалась. Возможно, пользователь уже существует.");
                    }
                })
                .onErrorResume(e -> {
                    log.error("Ошибка при регистрации: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Внутренняя ошибка сервера: " + e.getMessage()));
                });
    }


    @PostMapping("/login")
    public Mono<ResponseEntity<ValidateResponse>> login(ServerHttpRequest request, @RequestBody @Validated LoginRequestDto login) {
        return authService.auth(login, request);
    }

    @PostMapping(value = "/generate", produces = MediaType.IMAGE_PNG_VALUE)
    public Mono<ResponseEntity<byte[]>> generateQrCode(@RequestBody @Validated EmailDto email) {
        return authService.getQrCode(email);
    }

    @PostMapping("/login/otp")
    public Mono<ResponseEntity<Boolean>> otp(@RequestBody @Validated CodeVerification codeVerification) {
        return authService.validateCred(codeVerification).map(response -> ResponseEntity.ok().body(response));
    }

    @PostMapping("/login/validate")
    public Mono<Boolean> validate(@RequestBody String jwt, ServerHttpRequest request) {
        return authService.validate(request, jwt);
    }

    @PostMapping("/valid2fa")
    public Mono<ResponseEntity<Boolean>> valid2fa(@RequestBody @Validated CodeVerification codeVerification) {
        return authService.valid2fa(codeVerification).map(response -> ResponseEntity.ok().body(response));
    }

}
