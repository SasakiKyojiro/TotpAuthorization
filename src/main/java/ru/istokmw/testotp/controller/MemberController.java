package ru.istokmw.testotp.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;
import ru.istokmw.testotp.service.MemberService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@Slf4j
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
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


}
