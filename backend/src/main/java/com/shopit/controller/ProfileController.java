package com.shopit.controller;
import com.shopit.dto.ApiDtos.*;
import com.shopit.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileService profiles;
    public ProfileController(ProfileService profiles) { this.profiles = profiles; }

    @GetMapping
    public UserView get(Authentication authentication) { return profiles.get(authentication.getName()); }

    @PutMapping
    public UserView update(Authentication authentication, @Valid @RequestBody ProfileUpdateRequest request) {
        return profiles.update(authentication.getName(), request);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(Authentication authentication,
                                               @Valid @RequestBody PasswordChangeRequest request) {
        profiles.changePassword(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
