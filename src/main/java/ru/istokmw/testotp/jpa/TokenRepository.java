package ru.istokmw.testotp.jpa;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface TokenRepository extends R2dbcRepository<TokenMember, UUID> {
    Mono<TokenMember> findByRefreshToken(String token);

    Mono<TokenMember> findByUserId(UUID userId);

    @Query("update auth.member_token t set refresh_token = :refreshToken where token_id = :tokenId")
    Mono<Void> updateRefreshTokenByTokenId(String refreshToken, UUID tokenId);

}
