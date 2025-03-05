package ru.istokmw.testotp.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.istokmw.testotp.dto.LoginRequestDto;
import ru.istokmw.testotp.integration.TotpManager;
import ru.istokmw.testotp.jpa.Member;
import ru.istokmw.testotp.jpa.TOTP;
import ru.istokmw.testotp.jpa.TotpRepository;
import ru.istokmw.testotp.jpa.UserRepository;

import java.util.UUID;

@Service
@Slf4j
public class TotpService {
    private final TotpManager totpManager;
    private final TotpRepository totpRepository;
    private final UserRepository userRepository;

    public TotpService(TotpManager totpManager, TotpRepository totpRepository, UserRepository userRepository) {
        this.totpManager = totpManager;
        this.totpRepository = totpRepository;
        this.userRepository = userRepository;
    }

    public Mono<TOTP> findById(UUID id) {
        return totpRepository.findById(id);
    }

    public Mono<Member> findMemberByName(String name) {
        log.info("find member by name: {}", name);
        return userRepository.findById(userRepository.findIdByName(name));
    }

    public String generateSecret() {
        return totpManager.generateSecret();
    }

    public boolean isTotpValid(String secret, String code) {
        return totpManager.verifyCode(secret, code);
    }

    public Mono<Boolean> register(LoginRequestDto loginRequestDto) {
        return userRepository.insertMember(loginRequestDto.email(), loginRequestDto.password()).defaultIfEmpty(false);
    }
}
