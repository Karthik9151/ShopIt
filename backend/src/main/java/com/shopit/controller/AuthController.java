package com.shopit.controller;
import com.shopit.dto.ApiDtos.*;
import com.shopit.service.AuthService;
import com.shopit.security.RateLimitService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import java.time.Duration;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/auth")
public class AuthController{
 private final AuthService auth;
 private final RateLimitService limits;
 public AuthController(AuthService a, RateLimitService limits){auth=a;this.limits=limits;}
 @PostMapping("/register") public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(auth.register(r));}
 @PostMapping("/login")
 public ResponseEntity<?> login(@Valid @RequestBody LoginRequest r) {
  RateLimitService.Decision decision = limits.consumeLoginEmail(r.email());
  if (!decision.allowed()) {
   return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
    .header(HttpHeaders.RETRY_AFTER, Long.toString(decision.retryAfterSeconds()))
    .body(Map.of("message", "Too many login attempts for this account. Try again later."));
  }
  return ResponseEntity.ok(auth.login(r));
 }
 @PostMapping("/logout") public ResponseEntity<Void> logout(){return ResponseEntity.noContent().build();}
}