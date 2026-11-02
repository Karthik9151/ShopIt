package com.shopit.service;

import com.shopit.entity.AppUser;
import com.shopit.repository.UserRepository;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String email;
    private final String password;
    private final boolean takeOverExisting;

    public AdminBootstrap(
            UserRepository users,
            PasswordEncoder encoder,
            @Value("${app.admin.email:}") String email,
            @Value("${app.admin.password:}") String password,
            @Value("${app.admin.take-over-existing:false}") boolean takeOverExisting) {
        this.users = users;
        this.encoder = encoder;
        this.email = email;
        this.password = password;
        this.takeOverExisting = takeOverExisting;
    }

    @Override
    public void run(ApplicationArguments args) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.isBlank()) {
            log.info("Admin bootstrap: not configured; no administrator account was changed");
            return;
        }

        if (password == null || password.length() < 12) {
            log.warn("Admin bootstrap: password shorter than 12 characters; no administrator account was changed");
            return;
        }

        Optional<AppUser> existing = users.findByEmailIgnoreCase(normalizedEmail);
        if (existing.isPresent()) {
            AppUser user = existing.get();
            if (!takeOverExisting) {
                log.info(
                        "Admin bootstrap: account {} exists with role {} and was left untouched",
                        normalizedEmail,
                        user.getRole());
                return;
            }

            // Without email verification, this account may have been registered by someone else.
            user.setRole("ADMIN");
            user.setEnabled(true);
            user.setPasswordHash(encoder.encode(password));
            users.save(user);
            log.warn("Admin bootstrap: existing account {} was promoted to ADMIN and its password was replaced", normalizedEmail);
            return;
        }

        AppUser admin = new AppUser();
        admin.setName("ShopIt Administrator");
        admin.setEmail(normalizedEmail);
        admin.setPasswordHash(encoder.encode(password));
        admin.setRole("ADMIN");
        admin.setEnabled(true);
        users.save(admin);
        log.info("Admin bootstrap: administrator account {} was created", normalizedEmail);
    }

    private String normalizeEmail(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
