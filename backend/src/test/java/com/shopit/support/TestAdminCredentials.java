package com.shopit.support;

/**
 * Provides the administrator identity provisioned by the CI application profile.
 * Values come from the process environment and are never embedded in test sources.
 */
public final class TestAdminCredentials {
    private TestAdminCredentials() {}

    public static String email() {
        return required("APP_ADMIN_EMAIL");
    }

    public static String password() {
        return required("APP_ADMIN_PASSWORD");
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required test environment variable is not set: " + name);
        }
        return value;
    }
}
