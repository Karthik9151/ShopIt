package com.shopit.service;
import com.shopit.dto.ApiDtos.*;
import com.shopit.entity.AppUser;
import com.shopit.repository.UserRepository;
import com.shopit.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager manager;
    private final JwtService jwt;
    private final long expiry;

    public AuthService(UserRepository users, PasswordEncoder encoder, AuthenticationManager manager,
                       JwtService jwt, @Value("${app.jwt.expiration-ms}") long expiry) {
        this.users = users;
        this.encoder = encoder;
        this.manager = manager;
        this.jwt = jwt;
        this.expiry = expiry;
    }

    public AuthResponse register(RegisterRequest request) {
        if (users.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        AppUser user = new AppUser();
        user.setName(request.name().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(encoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setRole("CUSTOMER");
        return response(users.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        try {
            manager.authenticate(new UsernamePasswordAuthenticationToken(
                request.email().trim().toLowerCase(), request.password()));
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        AppUser user = users.findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return response(user);
    }

    private AuthResponse response(AppUser user) {
        return new AuthResponse(jwt.generateToken(user),
            new UserView(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getPhone()), expiry);
    }
}
