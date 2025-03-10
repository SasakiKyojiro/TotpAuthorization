package ru.istokmw.testotp.service;

import dev.samstevens.totp.exceptions.QrGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.istokmw.testotp.dto.CodeVerification;
import ru.istokmw.testotp.dto.EmailDto;
import ru.istokmw.testotp.dto.LoginRequestDto;
import ru.istokmw.testotp.dto.ValidateResponse;
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


    public Mono<ValidateResponse> auth(LoginRequestDto login) {
        return userRepository.findIdByName(login.email())
                .flatMap(userRepository::findById)
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull(member -> {
                    if (login.password().equals(member.getPassword())) {
                        return ValidateResponse.builder()
                                .success(true)
                                .f2pa(totpRepository.findEnabledById(member.getId()).block())
                                .build();
                    } else {
                        return ValidateResponse.builder()
                                .success(false)
                                .message("Username or password is incorrect")
                                .build();

                    }
                })
                .defaultIfEmpty(
                        ValidateResponse.builder()
                                .success(Boolean.FALSE)
                                .message("User not found")
                                .build()
                );
    }

    public Mono<Boolean> validateCred(CodeVerification codeVerification) {
        return totpManager.verifyCode(
                totpRepository.getSecret(codeVerification.email()),
                codeVerification.code()
        );
    }


    public Mono<ResponseEntity<byte[]>> getQrCode(EmailDto login) {
        log.info("getQrCode login: {}", login.email());
        return userRepository.findIdByName(login.email())
                .switchIfEmpty(Mono.error(new RuntimeException("ID not found for user: " + login.email())))
                .flatMap(uuid -> totpRepository.getSecret(login.email()))
                .flatMap(row -> {
                    try {
                        return totpManager.getQrCode(login.email(), row)
                                .flatMap(totpManager::getImage);
                    } catch (QrGenerationException e) {
                        log.error("getQrCode error {}", e.getMessage());
                        return Mono.error(new RuntimeException("generate qr code"));
                    }
                })
                .doOnError(e -> log.error("Error occurred: ", e));


    }
}
