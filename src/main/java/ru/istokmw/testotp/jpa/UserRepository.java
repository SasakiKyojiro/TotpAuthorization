package ru.istokmw.testotp.jpa;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserRepository extends R2dbcRepository<Member, UUID> {

    Mono<Member> findById(UUID id);

    Mono<Member> findByName(String username);

    @Query("INSERT INTO auth.member (name, password_hash) " +
            "VALUES (:login, :password) " +
            "ON CONFLICT (name) DO NOTHING " +
            "RETURNING true AS success")
    Mono<Boolean> insertMember(String login, String password);

    @Query("SELECT id FROM auth.member Where name=:username")
    Mono<UUID> findIdByName(String username);

    @Query("DELETE FROM auth.member WHERE id = :id ")
    Mono<Void> deleteById(UUID id);

    @Query("SELECT m.id, m.name, m.password_hash, array_agg(a.role) AS roles " +
            "FROM auth.member m \n" +
            "JOIN auth.member_authorities ma ON m.id = ma.\"userId\" \n" +
            "JOIN auth.authorities a ON ma.\"rolesId\" = a.id " +
            "WHERE (ma.active = true AND m.name=:username)  " +
            "GROUP BY m.id, m.name, m.password_hash")
    Mono<MemberAuth> findAuthByUsername(String username);
}
