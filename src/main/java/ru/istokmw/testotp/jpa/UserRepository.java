package ru.istokmw.testotp.jpa;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserRepository extends R2dbcRepository<Member, UUID> {

    Mono<Member> findById(UUID id);

    @Query("INSERT INTO auth.member (name, password_hash) " +
            "VALUES (:login, :password) " +
            "ON CONFLICT (name) DO NOTHING " +
            "RETURNING true AS success")
    Mono<Boolean> insertMember(String login, String password);

    @Query("SELECT id FROM auth.member Where name=:username")
    Mono<UUID> findIdByName(String username);

}
