package com.shopit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.shopit.dto.ApiDtos.AuthResponse;
import com.shopit.dto.ApiDtos.RegisterRequest;
import com.shopit.entity.AppUser;
import com.shopit.repository.UserRepository;
import com.shopit.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceRegistrationTest {
    @Mock UserRepository users;
    @Mock PasswordEncoder encoder;
    @Mock AuthenticationManager manager;
    @Mock JwtService jwt;

    private AuthService auth;

    @BeforeEach
    void setUp() {
        auth = new AuthService(users, encoder, manager, jwt, 900000L, "admin@shopit.test");
    }

    @Test
    void rejectsPasswordThatMatchesNormalizedEmail() {
        assertStatus(
                () -> auth.register(new RegisterRequest(
                        "Customer", "Customer@Example.test", "customer@example.test", null)),
                HttpStatus.BAD_REQUEST);
        verifyNoInteractions(encoder);
        verify(users, never()).save(any());
    }

    @Test
    void rejectsCommonPasswords() {
        assertStatus(
                () -> auth.register(new RegisterRequest(
                        "Customer", "customer@example.test", "Password123", null)),
                HttpStatus.BAD_REQUEST);
        verifyNoInteractions(encoder);
        verify(users, never()).save(any());
    }

    @Test
    void duplicateEmailInsertRaceReturnsTheSameConflictAsPrecheck() {
        when(users.existsByEmailIgnoreCase("customer@example.test")).thenReturn(false, true);
        when(encoder.encode("Safe-Unique-Password-2026!")).thenReturn("encoded");
        when(users.save(any(AppUser.class))).thenThrow(new DataIntegrityViolationException("unique constraint"));

        assertStatus(
                () -> auth.register(new RegisterRequest(
                        " Customer ", " Customer@Example.test ", "Safe-Unique-Password-2026!", null)),
                HttpStatus.CONFLICT);

        verify(users).existsByEmailIgnoreCase("customer@example.test");
        verify(users).save(any(AppUser.class));
    }

    @Test
    void normalizedRegistrationEmailAndNameArePersisted() {
        when(users.existsByEmailIgnoreCase("customer@example.test")).thenReturn(false);
        when(encoder.encode("Safe-Unique-Password-2026!")).thenReturn("encoded");
        when(users.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser saved = invocation.getArgument(0);
            assertThat(saved.getName()).isEqualTo("Customer");
            assertThat(saved.getEmail()).isEqualTo("customer@example.test");
            return saved;
        });
        when(jwt.generateToken(any(AppUser.class))).thenReturn("not-a-real-token");

        AuthResponse response = auth.register(new RegisterRequest(
                " Customer ", " Customer@Example.test ", "Safe-Unique-Password-2026!", null));

        assertThat(response.user().email()).isEqualTo("customer@example.test");
        assertThat(response.user().name()).isEqualTo("Customer");
    }

    private void assertStatus(org.assertj.core.api.ThrowableAssert.ThrowingCallable action, HttpStatus status) {
        assertThatThrownBy(action)
                .isInstanceOf(ResponseStatusException.class)
                .extracting(error -> ((ResponseStatusException) error).getStatusCode())
                .isEqualTo(status);
    }
}
