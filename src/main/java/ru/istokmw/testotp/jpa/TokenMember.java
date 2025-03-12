package ru.istokmw.testotp.jpa;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Table(name = "member_token", schema = "auth")
public class TokenMember {
    @Id
    @Column(value = "token_id")
    private UUID tokenId;
    @Column(value = "user_id")
    private UUID userId;
    @Column(value = "refresh_token")
    private String refreshToken;
    @Column(value = "expires_at")
    private LocalDate expiresAt;
    @Column(value = "created_at")
    private LocalDateTime createdAt;

}
