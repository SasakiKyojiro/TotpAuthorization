package ru.istokmw.testotp.jpa;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface TotpRepository extends R2dbcRepository<TOTP, UUID> {
    Mono<TOTP> findById(UUID userId);

    @Query("INSERT INTO auth.totp (user_id, secret, recovery_codes) VALUES (:id, :secret, :recoveryCodes) " +
            "ON CONFLICT (user_id) DO NOTHING " +
            "RETURNING true AS success")
    Mono<Boolean> insert(UUID id, String secret, String[] recoveryCodes);
}
