package ru.istokmw.testotp.jpa;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface AuthRepository extends R2dbcRepository<MemberAuth, UUID> {
    Mono<MemberAuth> findByUsername(String username);

    @Query("REFRESH MATERIALIZED VIEW auth.materialized_member_auth_info")
    Mono<Void> updateTable();
}
