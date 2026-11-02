package com.shopit.service;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class OrderStatusTransitions {
    private static final Map<String, List<String>> ALLOWED = Map.of(
            "PENDING_PAYMENT", List.of("CONFIRMED", "CANCELLED"),
            "CONFIRMED", List.of("PROCESSING"),
            "PROCESSING", List.of("SHIPPED"),
            "SHIPPED", List.of("DELIVERED"),
            "CANCELLED", List.of(),
            "DELIVERED", List.of());

    public List<String> allowedNextStatuses(String currentStatus, boolean successfulPaymentExists) {
        List<String> allowed = ALLOWED.getOrDefault(currentStatus, List.of());
        if ("PENDING_PAYMENT".equals(currentStatus)) {
            return successfulPaymentExists ? List.of("CONFIRMED") : List.of("CANCELLED");
        }
        return allowed;
    }

    public boolean isAllowed(String currentStatus, String nextStatus) {
        return ALLOWED.getOrDefault(currentStatus, List.of()).contains(nextStatus);
    }

    public void validateTransition(
            String currentStatus, String nextStatus, boolean successfulPaymentExists) {
        if (!isAllowed(currentStatus, nextStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Order cannot transition from " + currentStatus + " to " + nextStatus);
        }
        if ("PENDING_PAYMENT".equals(currentStatus)
                && "CONFIRMED".equals(nextStatus)
                && !successfulPaymentExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An order can be confirmed only after a successful payment");
        }
    }
}
