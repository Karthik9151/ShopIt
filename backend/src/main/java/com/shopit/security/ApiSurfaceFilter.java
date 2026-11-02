package com.shopit.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

/** Applies baseline API response headers and hides API documentation unless explicitly enabled. */
public class ApiSurfaceFilter extends OncePerRequestFilter {
    private final boolean apiDocsEnabled;

    public ApiSurfaceFilter(boolean apiDocsEnabled) {
        this.apiDocsEnabled = apiDocsEnabled;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");

        if (!apiDocsEnabled && isDocumentationPath(request.getRequestURI())) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Not found\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isDocumentationPath(String path) {
        return "/v3/api-docs".equals(path)
                || path.startsWith("/v3/api-docs/")
                || "/swagger-ui.html".equals(path)
                || path.startsWith("/swagger-ui/");
    }
}
