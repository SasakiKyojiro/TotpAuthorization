package ru.istokmw.testotp.config;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import ru.istokmw.testotp.util.ServerHttpRequestHelper;

@Slf4j
@Component
public class TokenAuthenticationFilter implements WebFilter {
    private final AuthenticationManagerService authenticationManager;
    private final JwtTokenProvider provider;

    public TokenAuthenticationFilter(AuthenticationManagerService authenticationManager, JwtTokenProvider provider) {
        this.authenticationManager = authenticationManager;
        this.provider = provider;
    }

    @Override
    @Nonnull
    public Mono<Void> filter(ServerWebExchange exchange, @Nonnull WebFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (token == null || !token.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }
        String authToken = token.substring(7);
        String clientIp = ServerHttpRequestHelper.getClientIp(exchange.getRequest());
        return provider.validateToken(authToken, clientIp)
                .flatMap(valid -> {
                    if (valid) {
                        String username = provider.getUsernameFromToken(authToken);
                        Mono<UserDetails> userDetailsMono = authenticationManager.getUser(username);
                        return userDetailsMono.flatMap(userDetails -> {
                            Authentication authentication = new UsernamePasswordAuthenticationToken(
                                    userDetails.getUsername(),
                                    null,
                                    userDetails.getAuthorities()
                            );
                            return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                        });
                    } else {
                        return Mono.error(new BadCredentialsException("Invalid JWT token"));
                    }
                })
                .onErrorResume(BadCredentialsException.class, ex -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    DataBuffer dataBuffer = exchange.getResponse().bufferFactory()
                            .wrap("Invalid JWT token".getBytes());
                    return exchange.getResponse().writeWith(Mono.just(dataBuffer));
                })
                .then();
    }
}
