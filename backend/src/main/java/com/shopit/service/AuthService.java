package com.shopit.service;

import com.shopit.dto.ApiDtos.*;
import com.shopit.entity.AppUser;
import com.shopit.repository.UserRepository;
import com.shopit.security.JwtService;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private static final String DUPLICATE_EMAIL_MESSAGE = "An account with this email already exists";

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager manager;
    private final JwtService jwt;
    private final long expiry;
    private final String adminEmail;

    public AuthService(
            UserRepository users,
            PasswordEncoder encoder,
            AuthenticationManager manager,
            JwtService jwt,
            @Value("${app.jwt.expiration-ms}") long expiry,
            @Value("${app.admin.email:}") String adminEmail) {
        this.users = users;
        this.encoder = encoder;
        this.manager = manager;
        this.jwt = jwt;
        this.expiry = expiry;
        this.adminEmail = normalizeEmail(adminEmail);
    }

    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (!adminEmail.isBlank() && adminEmail.equals(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, DUPLICATE_EMAIL_MESSAGE);
        }
        if (users.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, DUPLICATE_EMAIL_MESSAGE);
        }

        AppUser user = new AppUser();
        user.setName(request.name().trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(encoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setRole("CUSTOMER");
        return response(users.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        try {
            manager.authenticate(new UsernamePasswordAuthenticationToken(normalizedEmail, request.password()));
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        AppUser user = users.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return response(user);
    }

    private AuthResponse response(AppUser user) {
        return new AuthResponse(
                jwt.generateToken(user),
                new UserView(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getPhone()),
                expiry);
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
