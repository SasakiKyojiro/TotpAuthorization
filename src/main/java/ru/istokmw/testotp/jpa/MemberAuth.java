package ru.istokmw.testotp.jpa;

import lombok.Data;
import ru.istokmw.testotp.enums.Role;

import java.util.UUID;

@Data
public class MemberAuth {
    private UUID id;
    private String username;
    private String password;
    private Role[] roles;
}
