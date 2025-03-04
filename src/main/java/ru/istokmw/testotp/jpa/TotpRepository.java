package ru.istokmw.testotp.jpa;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface TotpRepository extends R2dbcRepository<TOTP, UUID> {
    Mono<TOTP> findById(UUID userId);
}
