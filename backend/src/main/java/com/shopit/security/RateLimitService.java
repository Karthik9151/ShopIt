package com.shopit.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Process-local token-bucket limits for sensitive account and commerce operations.
 * Buckets are intentionally not persisted: service restarts reset the counters.
 */
@Service
public class RateLimitService {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final boolean enabled;
    private final long idleTimeoutNanos;
    private final int maxBuckets;

    public RateLimitService(
            @Value("${app.rate-limit.enabled:true}") boolean enabled,
            @Value("${app.rate-limit.bucket-idle-minutes:30}") long bucketIdleMinutes,
            @Value("${app.rate-limit.max-buckets:100000}") int maxBuckets) {
        this.enabled = enabled;
        this.idleTimeoutNanos = Duration.ofMinutes(Math.max(1, bucketIdleMinutes)).toNanos();
        this.maxBuckets = Math.max(1000, maxBuckets);
    }

    public Decision consumeLoginEmail(String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase(java.util.Locale.ROOT);
        return consume("login-email:" + sha256(normalized), 5, Duration.ofMinutes(1));
    }

    public Decision consume(String key, int capacity, Duration refillPeriod) {
        if (!enabled) {
            return Decision.permitted();
        }
        if (key == null || key.isBlank() || capacity < 1 || refillPeriod.isZero() || refillPeriod.isNegative()) {
            throw new IllegalArgumentException("A rate-limit key, positive capacity and refill period are required");
        }

        long now = System.nanoTime();
        Bucket bucket = buckets.get(key);
        if (bucket == null) {
            if (buckets.size() >= maxBuckets) {
                evictIdleBuckets();
            }
            if (buckets.size() >= maxBuckets) {
                return Decision.rejected(60);
            }
            bucket = buckets.computeIfAbsent(key,
                    ignored -> new Bucket(capacity, refillPeriod.toNanos(), now));
        }

        synchronized (bucket) {
            bucket.refill(now);
            bucket.lastAccessNanos = now;
            if (bucket.tokens < 1.0d) {
                double missing = 1.0d - bucket.tokens;
                long waitNanos = (long) Math.ceil(missing * bucket.refillPeriodNanos / bucket.capacity);
                long retryAfter = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(waitNanos - 1) + 1);
                return Decision.rejected(retryAfter);
            }
            bucket.tokens -= 1.0d;
            return Decision.permitted();
        }
    }

    @Scheduled(fixedDelayString = "${app.rate-limit.eviction-interval-ms:300000}")
    public void evictIdleBuckets() {
        long now = System.nanoTime();
        buckets.forEach((key, bucket) -> {
            synchronized (bucket) {
                if (now - bucket.lastAccessNanos > idleTimeoutNanos) {
                    buckets.remove(key, bucket);
                }
            }
        });
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public record Decision(boolean allowed, long retryAfterSeconds) {
        static Decision permitted() {
            return new Decision(true, 0);
        }

        static Decision rejected(long retryAfterSeconds) {
            return new Decision(false, retryAfterSeconds);
        }
    }

    private static final class Bucket {
        private final int capacity;
        private final long refillPeriodNanos;
        private double tokens;
        private long lastRefillNanos;
        private long lastAccessNanos;

        private Bucket(int capacity, long refillPeriodNanos, long now) {
            this.capacity = capacity;
            this.refillPeriodNanos = refillPeriodNanos;
            this.tokens = capacity;
            this.lastRefillNanos = now;
            this.lastAccessNanos = now;
        }

        private void refill(long now) {
            long elapsed = Math.max(0, now - lastRefillNanos);
            if (elapsed == 0) {
                return;
            }
            double refill = (double) elapsed * capacity / refillPeriodNanos;
            tokens = Math.min(capacity, tokens + refill);
            lastRefillNanos = now;
        }
    }
}
