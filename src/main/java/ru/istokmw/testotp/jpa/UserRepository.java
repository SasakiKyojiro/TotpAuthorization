package ru.istokmw.testotp.jpa;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserRepository extends R2dbcRepository<Member, UUID> {

    Mono<Member> findById(UUID id);

    @Query("SELECT id FROM auth.member Where name=:username")
    Mono<UUID> findIdByName(String username);

}
