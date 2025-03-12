package ru.istokmw.testotp.util;

import org.springframework.http.server.reactive.ServerHttpRequest;

public class ServerHttpRequestHelper {
    public static String getClientIp(ServerHttpRequest request) {
        String forwardedHeader = request.getHeaders().getFirst("X-Forwarded-For");
        if (forwardedHeader != null && !forwardedHeader.isEmpty()) {
            return forwardedHeader.split(",")[0].trim();
        }
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown";
    }
}
