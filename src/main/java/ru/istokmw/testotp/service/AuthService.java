package ru.istokmw.testotp.service;

import dev.samstevens.totp.exceptions.QrGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.istokmw.testotp.config.JwtTokenProvider;
import ru.istokmw.testotp.dto.*;
import ru.istokmw.testotp.integration.TotpManager;
import ru.istokmw.testotp.jpa.TotpRepository;
import ru.istokmw.testotp.jpa.UserRepository;
import ru.istokmw.testotp.util.ServerHttpRequestHelper;

import java.time.LocalDate;

@Slf4j
@Service
public class AuthService {
    private final TotpManager totpManager;
    private final TotpRepository totpRepository;
    private final UserRepository userRepository;
    private final ReactiveAuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder encoder;

    public AuthService(TotpManager totpManager, TotpRepository totpRepository, UserRepository userRepository, ReactiveAuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.totpManager = totpManager;
        this.totpRepository = totpRepository;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.encoder = new BCryptPasswordEncoder();
    }


    public Mono<ValidateResponse> auth(LoginRequestDto login, ServerHttpRequest request) {
        String clientIp = ServerHttpRequestHelper.getClientIp(request);
        return authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(login.email(), login.password()))
                .flatMap(authentication -> {
                    String token = jwtTokenProvider.generateToken(authentication, clientIp);
                    log.info("token generated: {}", token);
                    JwtResponse jwtResponse = new JwtResponse(token);
                    return Mono.just(ValidateResponse.builder()
                            .token(jwtResponse)
                            .success(true)
                            .build());
                });

//        return userRepository.findIdByName(login.email())
//                .flatMap(userRepository::findById)
//                .publishOn(Schedulers.boundedElastic())
//                .mapNotNull(member -> {
//                    if (login.password().equals(member.getPassword())) {
//                        return ValidateResponse.builder()
//                                .success(true)
//                                .f2pa(totpRepository.findEnabledById(member.getId()).block())
//                                .build();
//                    } else {
//                        return ValidateResponse.builder()
//                                .success(false)
//                                .message("Username or password is incorrect")
//                                .build();
//
//                    }
//                })
//                .defaultIfEmpty(
//                        ValidateResponse.builder()
//                                .success(Boolean.FALSE)
//                                .message("User not found")
//                                .build()
//                );
    }

    public Mono<Boolean> validateCred(CodeVerification codeVerification) {
        return totpManager.verifyCode(
                        totpRepository.findSecretByUserName(codeVerification.email()),
                        codeVerification.code()
                ).publishOn(Schedulers.boundedElastic())
                .map(response -> {
                            if (response)
                                totpRepository
                                        .updateLastUsedById(userRepository.findIdByName(codeVerification.email()), LocalDate.now())
                                        .then()
                                        .subscribe();
                            return response;
                        }
                );
    }

    public Mono<Boolean> valid2fa(CodeVerification codeVerification) {
        return totpManager.verifyCode(
                        totpRepository.findSecretByUserName(codeVerification.email()),
                        codeVerification.code()
                )
                .publishOn(Schedulers.boundedElastic())
                .map(response -> {
                    if (response) {
                        totpRepository.updateIssuedAtById(userRepository.findIdByName(codeVerification.email()), LocalDate.now())
                                .then()
                                .subscribe();
                    }
                    return response;
                });
    }

    public Mono<ResponseEntity<byte[]>> getQrCode(EmailDto login) {
        log.info("getQrCode login: {}", login.email());
        return userRepository.findIdByName(login.email())
                .switchIfEmpty(Mono.error(new RuntimeException("ID not found for user: " + login.email())))
                .flatMap(uuid -> totpRepository.findSecretByUserName(login.email()))
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
