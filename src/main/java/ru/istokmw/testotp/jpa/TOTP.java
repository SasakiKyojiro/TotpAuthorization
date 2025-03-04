package ru.istokmw.testotp.jpa;



import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;
import java.util.UUID;

@Data
@Table(name = "totp", schema = "auth")
public class TOTP {
    @Id
    @Column(value = "user_id")
    private UUID id;

    @Column(value = "secret")
    private String secret;

    @Column(value = "issued_at")
    private Date issuedAt;
    @Column(value = "last_used")
    private Date lastUsed;

    @Column(value = "recovery_codes")
    private String recoveryCodes;

}
