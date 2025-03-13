package ru.istokmw.testotp.jpa;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import ru.istokmw.testotp.enums.Role;

import java.util.UUID;

@Data
@Table(name = "materialized_member_auth_info", schema = "auth")
public class MemberAuth {
    @Id
    @Column(value = "id")
    private UUID id;
    @Column(value = "name")
    private String username;
    @Column(value = "password_hash")
    private String password;
    @Column(value = "roles")
    private Role[] roles;
}
