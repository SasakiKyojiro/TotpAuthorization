package ru.istokmw.testotp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.istokmw.testotp.jpa.AuthRepository;

import java.util.Arrays;

@Service
public class AuthenticationManagerService {
    private final PasswordEncoder passwordEncoder;
    private final ReactiveUserDetailsService userDetailsService;

    public AuthenticationManagerService(AuthRepository userRepository) {
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.userDetailsService = username -> userRepository.findByUsername(username)
                .map(userEntity -> User.withUsername(userEntity.getUsername())
                        .password(userEntity.getPassword())
                        .roles(Arrays.toString(userEntity.getRoles()))
                        .build());
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
