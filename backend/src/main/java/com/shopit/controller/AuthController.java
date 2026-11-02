package com.shopit.controller;
import com.shopit.dto.ApiDtos.*;
import com.shopit.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/auth")
public class AuthController{
 private final AuthService auth;public AuthController(AuthService a){auth=a;}
 @PostMapping("/register") public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(auth.register(r));}
 @PostMapping("/login") public AuthResponse login(@Valid @RequestBody LoginRequest r){return auth.login(r);}
 @PostMapping("/logout") public ResponseEntity<Void> logout(){return ResponseEntity.noContent().build();}
}