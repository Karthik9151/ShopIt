package com.shopit.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class RateLimitFilter extends OncePerRequestFilter {
    private static final Duration ONE_MINUTE = Duration.ofMinutes(1);
    private static final Duration TEN_MINUTES = Duration.ofMinutes(10);

    private final RateLimitService limits;
    private final boolean enabled;
    private final boolean trustForwardedFor;

    public RateLimitFilter(
            RateLimitService limits,
            @Value("${app.rate-limit.enabled:true}") boolean enabled,
            @Value("${app.rate-limit.trust-forwarded-for:false}") boolean trustForwardedFor) {
        this.limits = limits;
        this.enabled = enabled;
        this.trustForwardedFor = trustForwardedFor;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        if (!enabled) {
            chain.doFilter(request, response);
            return;
        }

        String method = request.getMethod();
        String path = request.getRequestURI();
        RateLimitService.Decision decision = null;

        if ("POST".equals(method) && "/api/auth/login".equals(path)) {
            decision = limits.consume("login-ip:" + clientIp(request), 10, ONE_MINUTE);
        } else if ("POST".equals(method) && "/api/auth/register".equals(path)) {
            decision = limits.consume("register-ip:" + clientIp(request), 5, Duration.ofHours(1));
        } else if ("POST".equals(method) && "/api/orders".equals(path)) {
            decision = authenticatedUser()
                    .map(user -> limits.consume("orders-user:" + user, 10, ONE_MINUTE))
                    .orElse(null);
        } else if ("PUT".equals(method) && "/api/profile/password".equals(path)) {
            decision = authenticatedUser()
                    .map(user -> limits.consume("password-user:" + user, 5, TEN_MINUTES))
                    .orElse(null);
        } else if ("POST".equals(method) && path.startsWith("/api/payments/simulate/")) {
            decision = authenticatedUser()
                    .map(user -> limits.consume("payment-user:" + user, 5, ONE_MINUTE))
                    .orElse(null);
        }

        if (decision != null && !decision.allowed()) {
            response.setStatus(429);
            response.setHeader("Retry-After", Long.toString(decision.retryAfterSeconds()));
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Too many requests. Please try again later.\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    private java.util.Optional<String> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(authentication.getName());
    }

    private String clientIp(HttpServletRequest request) {
        if (trustForwardedFor) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                String first = forwarded.split(",", 2)[0].trim();
                if (!first.isBlank()) {
                    return first.length() > 128 ? first.substring(0, 128) : first;
                }
            }
        }
        String remote = request.getRemoteAddr();
        return remote == null || remote.isBlank() ? "unknown" : remote;
    }
}
