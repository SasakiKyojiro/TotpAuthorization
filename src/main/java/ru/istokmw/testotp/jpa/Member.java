package ru.istokmw.testotp.jpa;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.UUID;


@Data
@Table(name = "member", schema = "auth")
public class Member {
    @Id
    @Column(value = "id")
    private UUID id;
    @Column(value = "name")
    private String name;
    @Column(value = "password_hash")
    private String password;
    @Column(value = "created_at")
    private LocalDate createdAt;
    @Column(value = "last_login")
    private LocalDate lastLogin;
}
