package ru.istokmw.testotp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/login")
    public Mono<ResponseEntity<ValidateResponse>> login(@RequestBody @Validated LoginRequestDto login) {
        return authService.auth(login).flatMap(response -> {
            if (response.getSuccess()) return Mono.just(ResponseEntity.ok().body(response));
            else return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response));
        });
    }

    @PostMapping(value = "/generate", produces = MediaType.IMAGE_PNG_VALUE)
    public Mono<ResponseEntity<byte[]>> generateQrCode(@RequestBody @Validated EmailDto email) {
        return authService.getQrCode(email);
    }

    @PostMapping("/login/otp")
    public Mono<ResponseEntity<Boolean>> otp(@RequestBody @Validated CodeVerification codeVerification) {
        return authService.validateCred(codeVerification).flatMap(response -> Mono.just(ResponseEntity.ok().body(response)));
    }

    @PostMapping("/valid2fa")
    public Mono<Boolean> valid2fa(@RequestBody @Validated CodeVerification codeVerification) {
        return authService.valid2fa(codeVerification).flatMap(response -> Mono.just(true));
    }
}
