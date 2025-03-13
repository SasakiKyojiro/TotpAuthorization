package ru.istokmw.testotp.service;

import dev.samstevens.totp.exceptions.QrGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.istokmw.testotp.config.JwtTokenProvider;
import ru.istokmw.testotp.dto.CodeVerification;
import ru.istokmw.testotp.dto.EmailDto;
import ru.istokmw.testotp.dto.LoginRequestDto;
import ru.istokmw.testotp.dto.ValidateResponse;
import ru.istokmw.testotp.integration.TotpManager;
import ru.istokmw.testotp.jpa.TotpRepository;
import ru.istokmw.testotp.jpa.UserRepository;
import ru.istokmw.testotp.util.ServerHttpRequestHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuthService {
    private final TotpManager totpManager;
    private final TotpRepository totpRepository;
    private final UserRepository userRepository;
    private final ReactiveAuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder encoder;

    private final String SC = "Set-Cookie";
    private final String domain;

    public AuthService(TotpManager totpManager, TotpRepository totpRepository, UserRepository userRepository, ReactiveAuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, @Value("${server.domain:localhost}") String domain) {
        this.totpManager = totpManager;
        this.totpRepository = totpRepository;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.domain = domain;
        this.encoder = new BCryptPasswordEncoder();
    }


    public Mono<ResponseEntity<ValidateResponse>> auth(LoginRequestDto login, ServerHttpRequest request) {
        String clientIp = ServerHttpRequestHelper.getClientIp(request);
        String username = login.email();
        return authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(username, login.password()))
                .flatMap(authentication ->
                        userRepository.findByName(username)
                                .flatMap(user -> totpRepository.findEnabledById(user.getId()))
                                .map(enable -> {
                                    if (enable) {
                                        return ValidateResponse.builder()
                                                .success(true)
                                                .f2pa(true)
                                                .build();
                                    } else return ValidateResponse.builder()
                                            .success(true)
                                            .f2pa(false)
                                            .build();
                                })
                                .map(response -> {
                                    if (!response.getF2pa()) {
                                        HttpHeaders headers = generateToken(username, authentication, clientIp);
                                        return ResponseEntity.ok().headers(headers).body(response);
                                    }
                                    return ResponseEntity.ok().body(response);
                                })
                )
                .doOnError(ex -> log.error("auth error: {}", ex.getMessage()))
                .onErrorReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        ValidateResponse.builder()
                                .success(false)
                                .message("Username or password is incorrect")
                                .build()));
    }


    public Mono<Boolean> validate(ServerHttpRequest request, String jwt) {
        return jwtTokenProvider.validateToken(jwt, ServerHttpRequestHelper.getClientIp(request));
    }


    public Mono<ResponseEntity<Boolean>> validateCred(CodeVerification codeVerification, ServerHttpRequest request) {
        String clientIp = ServerHttpRequestHelper.getClientIp(request);
        String username = codeVerification.email();
        return totpManager.verifyCode(totpRepository.findSecretByUserName(username), codeVerification.code())
                .flatMap(response -> {
                            log.info(response.toString());
                            if (response) {
                                return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, codeVerification.password()))
                                        .map(authentication -> {
                                            log.info(authentication.toString());
                                            HttpHeaders headers = generateToken(username, authentication, clientIp);
                                            return ResponseEntity.status(HttpStatus.OK).headers(headers).body(Boolean.TRUE);
                                        });
                            } else {
                                return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Boolean.FALSE));
                            }
                        }
                )
                .flatMap(response -> {
                            if (response.getStatusCode() == HttpStatus.OK)
                                return userRepository.updateLastLogin(LocalDateTime.now(), username).thenReturn(response);
                            return Mono.just(response);
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

    public Mono<Boolean> register(LoginRequestDto loginRequestDto) {
        String username = loginRequestDto.email();
        String password = encoder.encode(loginRequestDto.password());
        return userRepository.insertMember(username, password)
                .defaultIfEmpty(false)
                .publishOn(Schedulers.boundedElastic())
                .flatMap(result -> {
                    if (result) {
                        return userRepository.findIdByName(loginRequestDto.email())
                                .flatMap(userId -> totpRepository.insert(userId, totpManager.generateSecret(), totpManager.generateRecovery()))
                                .thenReturn(true);
                    }
                    return Mono.just(false);
                });
    }


    private HttpHeaders generateToken(String username, Authentication authentication, String clientIp) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", ""))
                .collect(Collectors.joining(","));
        HttpHeaders headers = new HttpHeaders();
        String token = jwtTokenProvider.generateToken(username, roles, clientIp);
        String domainFormat = String.format(" Path=/; Domain=.%s;", domain);
        headers.add(SC, String.format("userrole=%s;%s", roles, domainFormat));
        headers.add(SC, String.format("username=%s;%s", username, domainFormat));
        headers.add(SC, String.format("token=%s;%s", token, domainFormat));
        return headers;
    }
}
