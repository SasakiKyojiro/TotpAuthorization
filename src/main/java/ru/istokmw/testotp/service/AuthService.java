package ru.istokmw.testotp.service;

import dev.samstevens.totp.exceptions.QrGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.istokmw.testotp.dto.LoginRequestDto;
import ru.istokmw.testotp.integration.TotpManager;
import ru.istokmw.testotp.jpa.TotpRepository;
import ru.istokmw.testotp.jpa.UserRepository;

@Slf4j
@Service
public class AuthService {
    private final TotpManager totpManager;
    private final TotpRepository totpRepository;
    private final UserRepository userRepository;

    public AuthService(TotpManager totpManager, TotpRepository totpRepository, UserRepository userRepository) {
        this.totpManager = totpManager;
        this.totpRepository = totpRepository;
        this.userRepository = userRepository;
    }


    public Mono<Boolean> auth(LoginRequestDto login) {
        return userRepository.findIdByName(login.email())
                .flatMap(userRepository::findById)
                .map(member -> member.getPassword().equals(login.password()))
                .defaultIfEmpty(false);
    }

    public Mono<String> getQrCode(LoginRequestDto login) {
        log.info("getQrCode login: {}", login);
        return userRepository.findIdByName(login.email())
                .switchIfEmpty(Mono.error(new RuntimeException("ID not found for user: " + login.email())))
                .flatMap(id -> Mono.zip(
                        userRepository.findById(id),
                        totpRepository.findById(id)
                ))
                .<String>handle((tuple, sink) -> {
                    try {
                        sink.next(totpManager.getQrCode(tuple.getT1(), tuple.getT2()));
                    } catch (QrGenerationException e) {
                        sink.error(new RuntimeException(e));
                    }
                })
                .doOnError(e -> log.error("Error occurred: ", e));

    }
}
