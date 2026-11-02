package com.shopit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.shopit.entity.AppUser;
import com.shopit.repository.UserRepository;
import com.shopit.service.AdminBootstrap;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class AdminBootstrapTest {
    private static final String ADMIN_EMAIL = "owner@example.com";
    private static final String ADMIN_PASSWORD = "Long-Admin-Password-2026!";

    private final UserRepository users = org.mockito.Mockito.mock(UserRepository.class);
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);

    @Test
    void blankEmailDoesNotQueryOrChangeUsers() throws Exception {
        new AdminBootstrap(users, encoder, "   ", ADMIN_PASSWORD, false)
                .run(new DefaultApplicationArguments(new String[0]));

        verifyNoInteractions(users);
    }

    @Test
    void shortPasswordDoesNotQueryOrChangeUsers() throws Exception {
        new AdminBootstrap(users, encoder, ADMIN_EMAIL, "short", false)
                .run(new DefaultApplicationArguments(new String[0]));

        verifyNoInteractions(users);
    }

    @Test
    void createsNormalizedAdministratorWhenEmailIsNew() throws Exception {
        when(users.findByEmailIgnoreCase(ADMIN_EMAIL)).thenReturn(Optional.empty());
        when(users.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        new AdminBootstrap(users, encoder, "  OWNER@EXAMPLE.COM ", ADMIN_PASSWORD, false)
                .run(new DefaultApplicationArguments(new String[0]));

        var captor = org.mockito.ArgumentCaptor.forClass(AppUser.class);
        verify(users).save(captor.capture());
        AppUser saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo(ADMIN_EMAIL);
        assertThat(saved.getRole()).isEqualTo("ADMIN");
        assertThat(saved.isEnabled()).isTrue();
        assertThat(saved.getName()).isEqualTo("ShopIt Administrator");
        assertThat(encoder.matches(ADMIN_PASSWORD, saved.getPasswordHash())).isTrue();
    }

    @Test
    void leavesExistingCustomerUntouchedWhenTakeoverIsDisabled() throws Exception {
        AppUser customer = existingCustomer();
        String oldHash = customer.getPasswordHash();
        when(users.findByEmailIgnoreCase(ADMIN_EMAIL)).thenReturn(Optional.of(customer));

        new AdminBootstrap(users, encoder, ADMIN_EMAIL, ADMIN_PASSWORD, false)
                .run(new DefaultApplicationArguments(new String[0]));

        assertThat(customer.getRole()).isEqualTo("CUSTOMER");
        assertThat(customer.isEnabled()).isFalse();
        assertThat(customer.getPasswordHash()).isEqualTo(oldHash);
        verify(users, never()).save(any(AppUser.class));
    }

    @Test
    void takeoverPromotesExistingAccountAndReplacesPassword() throws Exception {
        AppUser customer = existingCustomer();
        when(users.findByEmailIgnoreCase(ADMIN_EMAIL)).thenReturn(Optional.of(customer));
        when(users.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        new AdminBootstrap(users, encoder, ADMIN_EMAIL, ADMIN_PASSWORD, true)
                .run(new DefaultApplicationArguments(new String[0]));

        assertThat(customer.getRole()).isEqualTo("ADMIN");
        assertThat(customer.isEnabled()).isTrue();
        assertThat(encoder.matches(ADMIN_PASSWORD, customer.getPasswordHash())).isTrue();
        assertThat(encoder.matches("Old-Customer-Password", customer.getPasswordHash())).isFalse();
        verify(users).save(customer);
    }

    private AppUser existingCustomer() {
        AppUser customer = new AppUser();
        customer.setName("Existing Customer");
        customer.setEmail(ADMIN_EMAIL);
        customer.setPasswordHash(encoder.encode("Old-Customer-Password"));
        customer.setRole("CUSTOMER");
        customer.setEnabled(false);
        return customer;
    }
}
