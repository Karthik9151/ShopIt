package com.shopit.security;

import com.shopit.entity.AppUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Writes one metadata-only access log entry per request.
 *
 * Request bodies, query strings, headers, credentials, and user emails are
 * deliberately excluded from the log statement.
 */
public class RequestLogFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestLogFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        long startedAt = System.nanoTime();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMillis = (System.nanoTime() - startedAt) / 1_000_000L;
            Object traceValue = request.getAttribute(TraceIdFilter.TRACE_ID_ATTRIBUTE);
            String traceId = traceValue instanceof String value && !value.isBlank()
                    ? value
                    : "unknown";

            log.info(
                    "http_request method={} path={} status={} durationMs={} userId={} traceId={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMillis,
                    authenticatedUserId(),
                    traceId);
        }
    }

    private String authenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "anonymous";
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AppUser user && user.getId() != null) {
            return user.getId().toString();
        }
        return "unknown";
    }
}
