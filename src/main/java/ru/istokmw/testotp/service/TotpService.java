package ru.istokmw.testotp.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.istokmw.testotp.integration.TotpManager;
import ru.istokmw.testotp.jpa.TOTP;
import ru.istokmw.testotp.jpa.TotpRepository;
import ru.istokmw.testotp.jpa.UserRepository;

import java.util.UUID;

@Service
@Slf4j
public class TotpService {
    @Autowired
    private TotpManager totpManager;
    @Autowired
    private TotpRepository totpRepository;
    @Autowired
    private UserRepository userRepository;

    public Mono<TOTP> findById(UUID id) {
        return totpRepository.findById(id);
    }

    public Flux<UUID> findMemberByName(String name) {
        return userRepository.findIdByName(name);
    }

    public String generateSecret() {
        return totpManager.generateSecret();
    }

    public boolean isTotpValid(String secret, String code) {
        return totpManager.verifyCode(secret, code);
    }

}
