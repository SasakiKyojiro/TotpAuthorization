package ru.istokmw.testotp.controller;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.istokmw.testotp.jpa.Member;
import ru.istokmw.testotp.jpa.TOTP;
import ru.istokmw.testotp.service.TotpService;

import java.util.UUID;

@RestController
@RequestMapping("/")
public class ApiController {

    private final TotpService totpService;

    public ApiController(TotpService totpService) {
        this.totpService = totpService;
    }


    @PostMapping("/totp")
    public Mono<TOTP> findOtp(@RequestBody UUID uuid) {
        return totpService.findById(uuid);
    }

    @GetMapping("/member")
    public Mono<Member> findMemberByUsername(@RequestParam(name = "name") String username) {
        return totpService.findMemberByName(username);
    }
    

}
