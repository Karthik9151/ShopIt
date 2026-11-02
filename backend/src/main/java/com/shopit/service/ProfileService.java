package com.shopit.service;
import com.shopit.dto.ApiDtos.*;
import com.shopit.entity.AppUser;
import com.shopit.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ProfileService {
    private final UserRepository users;
    private final PasswordEncoder passwords;

    public ProfileService(UserRepository users, PasswordEncoder passwords) {
        this.users = users;
        this.passwords = passwords;
    }

    @Transactional(readOnly = true)
    public UserView get(String email) {
        AppUser user = user(email);
        return view(user);
    }

    public UserView update(String email, ProfileUpdateRequest request) {
        AppUser user = user(email);
        user.setName(request.name().trim());
        user.setPhone(request.phone());
        return view(user);
    }

    public void changePassword(String email, PasswordChangeRequest request) {
        AppUser user = user(email);
        if (!passwords.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }
        if (passwords.matches(request.newPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a password you have not used before");
        }
        user.setPasswordHash(passwords.encode(request.newPassword()));
    }

    private UserView view(AppUser user) {
        return new UserView(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    private AppUser user(String email) {
        return users.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
