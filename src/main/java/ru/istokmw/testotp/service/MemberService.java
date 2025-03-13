package ru.istokmw.testotp.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.istokmw.testotp.integration.TotpManager;
import ru.istokmw.testotp.jpa.Member;
import ru.istokmw.testotp.jpa.TotpRepository;
import ru.istokmw.testotp.jpa.UserRepository;

import java.util.UUID;

@Service
@Slf4j
public class MemberService {
    private final TotpManager totpManager;
    private final TotpRepository totpRepository;
    private final UserRepository userRepository;

    public MemberService(TotpManager totpManager, TotpRepository totpRepository, UserRepository userRepository) {
        this.totpManager = totpManager;
        this.totpRepository = totpRepository;
        this.userRepository = userRepository;
    }

    public Mono<ResponseEntity<Boolean>> create() {
        return Mono.just(ResponseEntity.ok().body(true));
    }

    public Mono<Void> updateMember(Member member) {
        return Mono.empty();
    }

    public Mono<Boolean> deleteMember(UUID id) {
        return userRepository.findById(id)
                .flatMap(user -> userRepository.deleteById(user.getId())
                        .then(Mono.just(true)))
                .defaultIfEmpty(false);

    }


}
