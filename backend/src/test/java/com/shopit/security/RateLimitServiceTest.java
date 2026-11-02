package com.shopit.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class RateLimitServiceTest {
    @Test
    void tokenBucketAllowsConfiguredBurstThenReturnsRetryAfter() {
        RateLimitService limits = new RateLimitService(true, 30, 1000);

        for (int index = 0; index < 5; index++) {
            assertThat(limits.consume("login-email:test", 5, Duration.ofMinutes(1)).allowed())
                    .isTrue();
        }

        RateLimitService.Decision limited =
                limits.consume("login-email:test", 5, Duration.ofMinutes(1));

        assertThat(limited.allowed()).isFalse();
        assertThat(limited.retryAfterSeconds()).isBetween(1L, 12L);
    }

    @Test
    void eachRateLimitKeyHasAnIndependentBucket() {
        RateLimitService limits = new RateLimitService(true, 30, 1000);

        for (int index = 0; index < 5; index++) {
            limits.consume("login-email:first", 5, Duration.ofMinutes(1));
        }

        assertThat(limits.consume("login-email:second", 5, Duration.ofMinutes(1)).allowed())
                .isTrue();
    }

    @Test
    void disabledRateLimitingDoesNotConsumeBuckets() {
        RateLimitService limits = new RateLimitService(false, 30, 1000);

        for (int index = 0; index < 20; index++) {
            assertThat(limits.consume("disabled", 1, Duration.ofMinutes(1)).allowed())
                    .isTrue();
        }
    }
}
