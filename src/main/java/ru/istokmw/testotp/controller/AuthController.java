package ru.istokmw.testotp.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.istokmw.testotp.dto.LoginRequestDto;
import ru.istokmw.testotp.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/login")
    public Mono<String> login(@RequestBody @Validated LoginRequestDto login) {
        return authService.auth(login)
                .map(isAuth -> {
                    if (isAuth) {
                        return authService.getQrCode(login);
                    } else
                        return Mono.just("Неверный логин или пароль");
                })
                .onErrorResume(e -> Mono.just(Mono.just("Внутренняя ошибка сервера: " + e.getMessage())))
                .block();
    }
}
