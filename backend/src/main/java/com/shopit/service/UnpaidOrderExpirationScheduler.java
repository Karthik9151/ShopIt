package com.shopit.service;

import com.shopit.repository.OrderRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UnpaidOrderExpirationScheduler {
    private static final Logger log = LoggerFactory.getLogger(UnpaidOrderExpirationScheduler.class);

    private final OrderRepository orders;
    private final UnpaidOrderExpirationProcessor processor;
    private final Clock clock;
    private final long unpaidTtlMinutes;

    public UnpaidOrderExpirationScheduler(
            OrderRepository orders,
            UnpaidOrderExpirationProcessor processor,
            Clock clock,
            @Value("${app.orders.unpaid-ttl-minutes:30}") long unpaidTtlMinutes) {
        this.orders = orders;
        this.processor = processor;
        this.clock = clock;
        this.unpaidTtlMinutes = unpaidTtlMinutes;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void expireMissedOrdersOnStartup() {
        expireUnpaidOrders();
    }

    @Scheduled(
            fixedDelayString = "${app.orders.expiry-sweep-ms:300000}",
            initialDelayString = "${app.orders.expiry-sweep-ms:300000}")
    public void expireUnpaidOrders() {
        Instant cutoff = clock.instant().minus(unpaidTtlMinutes, ChronoUnit.MINUTES);
        List<Long> candidates = orders.findExpiredUnpaidOrderIds(cutoff);
        int expired = 0;

        for (Long orderId : candidates) {
            try {
                if (processor.expireIfUnpaidAndExpired(orderId, cutoff)) {
                    expired++;
                }
            } catch (RuntimeException exception) {
                // Each order has its own transaction; one damaged order must not stop the batch.
                log.error("Could not expire unpaid order id={}", orderId, exception);
            }
        }

        if (expired > 0) {
            log.info("Automatically cancelled {} unpaid order(s)", expired);
        }
    }
}
