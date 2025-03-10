package ru.istokmw.testotp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;
import ru.istokmw.testotp.dto.LoginRequestDto;
import ru.istokmw.testotp.jpa.Member;
import ru.istokmw.testotp.jpa.TOTP;
import ru.istokmw.testotp.service.MemberService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Validated LoginRequestDto loginRequestDto) {
        return memberService.register(loginRequestDto)
                .map(isRegistered -> {
                    if (isRegistered) {
                        return ResponseEntity
                                .ok("Регистрация прошла успешно!");
                    } else {
                        return ResponseEntity
                                .badRequest()
                                .body("Регистрация не удалась. Возможно, пользователь уже существует.");
                    }
                })
                .onErrorResume(e -> Mono
                        .just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Внутренняя ошибка сервера: " + e.getMessage()))).block();
    }

    @PostMapping("/delete")
    public ResponseEntity<String> deleteUser(@RequestBody UUID userId) {
        return memberService.deleteMember(userId)
                .map(isDeleting -> {
                    if (isDeleting)
                        return ResponseEntity
                                .ok("Пользователь успешно удалён");
                    else
                        return ResponseEntity
                                .badRequest()
                                .body("Удаление пользователя не удалось. Возможно пользователя с камим id нет.");
                })
                .onErrorResume(e -> Mono
                        .just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Внутренняя ошибка сервера: " + e.getMessage()))).block();
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleValidationExceptions(WebExchangeBindException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage())
                );
        return Mono.just(ResponseEntity
                .badRequest()
                .body(errors));
    }

    @PostMapping("/totp")
    public Mono<TOTP> findOtp(@RequestBody UUID uuid) {
        return memberService.findById(uuid);
    }

    @GetMapping("/member")
    public Mono<Member> findMemberByUsername(@RequestParam(name = "name") String username) {
        return memberService.findMemberByName(username);
    }


}
