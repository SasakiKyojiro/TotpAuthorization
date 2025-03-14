package ru.istokmw.testotp.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;


@Slf4j
@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {
    private final AuthenticationManagerService authenticationManagerService;
    private final TokenAuthenticationFilter tokenAuthenticationFilter;

    public SecurityConfiguration(AuthenticationManagerService authenticationManagerService, TokenAuthenticationFilter tokenAuthenticationFilter) {
        this.authenticationManagerService = authenticationManagerService;
        this.tokenAuthenticationFilter = tokenAuthenticationFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.addAllowedOriginPattern("*");
                    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(Collections.singletonList("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(authorizeExchangeSpec -> {
                    authorizeExchangeSpec.pathMatchers(
                                    "/auth/login",
                                    "/auth/login/**",
                                    "/auth/register",
                                    "/auth/generate"
                            ).permitAll()
                            .pathMatchers("/test/admin/**").hasRole("ADMIN")
                            .pathMatchers("test/buyer/**").hasRole("BUYER")
                            .pathMatchers("test/distributer/**").hasRole("DISTRIBUTOR")
                            .pathMatchers("test/seller/**").hasRole("SELLER")
                            .pathMatchers("test/guest/**").hasRole("GUEST")
                            .pathMatchers("test/manufacture/**").hasRole("MANUFACTURER")
                            .anyExchange().authenticated();
                })
                .authenticationManager(authenticationManagerService.getAuthenticationManager())
                .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(authenticationEntryPoint()))
                .addFilterBefore(tokenAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable);
        return http.build();
    }

    private ServerAuthenticationEntryPoint authenticationEntryPoint() {
        return (exchange, ex) -> {
            log.error("Authentication error: {}", ex.getMessage());
            return Mono.empty();
        };
    }
}

