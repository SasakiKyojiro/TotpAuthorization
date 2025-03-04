package ru.istokmw.testotp.jpa;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserRepository extends R2dbcRepository<Member, UUID> {
    @Query("SELECT * FROM auth.member Where 'name'=:username")
    Mono<Member> findByName(String username);

    @Query("SELECT id FROM auth.member Where 'name'=$1")
    Flux<UUID> findIdByName(String username);

}
