package ru.istokmw.testotp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.istokmw.testotp.jpa.TOTP;
import ru.istokmw.testotp.service.TotpService;

import java.util.UUID;

@RestController
@RequestMapping("/")
public class ApiController {

    @Autowired
    private TotpService totpService;


    @PostMapping("/totp")
    public Mono<TOTP> findOtp(@RequestBody UUID uuid) {
        return totpService.findById(uuid);
    }

    @PostMapping("/member")
    public Flux<UUID> findMemberByUsername(@RequestBody String username) {
        return totpService.findMemberByName(username);
    }
}
