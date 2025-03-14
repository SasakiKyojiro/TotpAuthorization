package ru.istokmw.testotp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.istokmw.testotp.enums.Role;
import ru.istokmw.testotp.jpa.AuthRepository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthenticationManagerService {
    private final PasswordEncoder passwordEncoder;
    private final ReactiveUserDetailsService userDetailsService;

    public AuthenticationManagerService(AuthRepository userRepository) {
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.userDetailsService = username -> userRepository.findByUsername(username)
                .map(userEntity -> User.withUsername(userEntity.getUsername())
                        .password(userEntity.getPassword())
                        .authorities(parseAuthorities(userEntity.getRoles()))
                        .build());
    }

    private List<GrantedAuthority> parseAuthorities(Role[] roles) {
        return Arrays.stream(roles)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name().toUpperCase()))
                .collect(Collectors.toList());
    }

    @Bean
    public ReactiveAuthenticationManager getAuthenticationManager() {
        UserDetailsRepositoryReactiveAuthenticationManager manager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        manager.setPasswordEncoder(passwordEncoder);
        return manager;
    }

    public Mono<UserDetails> getUser(String name) {
        return userDetailsService.findByUsername(name);
    }
}
