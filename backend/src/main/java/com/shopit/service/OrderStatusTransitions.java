package com.shopit.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class OrderStatusTransitions {
    private static final List<String> PROGRESS_STATUSES = List.of(
            "PENDING_PAYMENT", "CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED");

    public List<String> allowedNextStatuses(String currentStatus, boolean successfulPaymentExists) {
        if ("PENDING_PAYMENT".equals(currentStatus)) {
            return successfulPaymentExists
                    ? PROGRESS_STATUSES.subList(1, PROGRESS_STATUSES.size())
                    : List.of("CANCELLED");
        }
        if (!successfulPaymentExists
                || "CANCELLED".equals(currentStatus)
                || "DELIVERED".equals(currentStatus)) {
            return List.of();
        }

        int currentIndex = PROGRESS_STATUSES.indexOf(currentStatus);
        if (currentIndex < 0 || currentIndex >= PROGRESS_STATUSES.size() - 1) {
            return List.of();
        }
        return PROGRESS_STATUSES.subList(currentIndex + 1, PROGRESS_STATUSES.size());
    }

    public boolean isAllowed(String currentStatus, String nextStatus) {
        if ("CANCELLED".equals(nextStatus)) {
            return "PENDING_PAYMENT".equals(currentStatus);
        }
        int currentIndex = PROGRESS_STATUSES.indexOf(currentStatus);
        int nextIndex = PROGRESS_STATUSES.indexOf(nextStatus);
        return currentIndex >= 0 && nextIndex > currentIndex;
    }

    public void validateTransition(
            String currentStatus, String nextStatus, boolean successfulPaymentExists) {
        if (!isAllowed(currentStatus, nextStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Order cannot transition from " + currentStatus + " to " + nextStatus);
        }
        if ("CANCELLED".equals(nextStatus)) {
            if (successfulPaymentExists) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Paid orders cannot be cancelled");
            }
            return;
        }
        if (!successfulPaymentExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An order can move into fulfillment only after a successful payment");
        }
    }
}
