package com.shopit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.shopit.service.OrderStatusTransitions;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class OrderStatusTransitionsTest {
    private final OrderStatusTransitions transitions = new OrderStatusTransitions();

    @Test
    void unpaidOrderCanOnlyBeCancelledUntilPaymentSucceeds() {
        assertThat(transitions.allowedNextStatuses("PENDING_PAYMENT", false))
                .containsExactly("CANCELLED");
        assertThat(transitions.allowedNextStatuses("PENDING_PAYMENT", true))
                .containsExactly("CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED");
        transitions.validateTransition("PENDING_PAYMENT", "CANCELLED", false);
        transitions.validateTransition("PENDING_PAYMENT", "SHIPPED", true);
    }

    @Test
    void adminsCanChooseAnyLaterFulfillmentStatusForPaidOrders() {
        assertThat(transitions.allowedNextStatuses("CONFIRMED", true))
                .containsExactly("PROCESSING", "SHIPPED", "DELIVERED");
        assertThat(transitions.allowedNextStatuses("PROCESSING", true))
                .containsExactly("SHIPPED", "DELIVERED");
        assertThat(transitions.allowedNextStatuses("SHIPPED", true))
                .containsExactly("DELIVERED");
    }

    @Test
    void cancelledAndDeliveredOrdersAreFinal() {
        assertThat(transitions.allowedNextStatuses("CANCELLED", false)).isEmpty();
        assertThat(transitions.allowedNextStatuses("DELIVERED", true)).isEmpty();
        assertThat(transitions.isAllowed("DELIVERED", "CANCELLED")).isFalse();
        assertThat(transitions.isAllowed("CANCELLED", "CONFIRMED")).isFalse();
    }

    @Test
    void rejectsBackwardAndUnsupportedTransitions() {
        List<String[]> illegal = List.of(
                new String[]{"CONFIRMED", "PENDING_PAYMENT"},
                new String[]{"PROCESSING", "CONFIRMED"},
                new String[]{"SHIPPED", "PROCESSING"},
                new String[]{"DELIVERED", "SHIPPED"},
                new String[]{"CANCELLED", "CONFIRMED"},
                new String[]{"CONFIRMED", "CANCELLED"},
                new String[]{"PENDING_PAYMENT", "UNKNOWN"});

        for (String[] transition : illegal) {
            assertThat(transitions.isAllowed(transition[0], transition[1]))
                    .as("%s -> %s", transition[0], transition[1])
                    .isFalse();
        }
    }

    @Test
    void rejectsEveryFulfillmentTransitionWithoutSuccessfulPaymentWith409() {
        assertThatThrownBy(() ->
                transitions.validateTransition("PENDING_PAYMENT", "CONFIRMED", false))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        assertThatThrownBy(() ->
                transitions.validateTransition("PENDING_PAYMENT", "SHIPPED", false))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void rejectsCancellingPaidOrdersWith409() {
        assertThatThrownBy(() ->
                transitions.validateTransition("PENDING_PAYMENT", "CANCELLED", true))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void acceptsDirectForwardUpdateForPaidOrder() {
        transitions.validateTransition("CONFIRMED", "SHIPPED", true);
        transitions.validateTransition("PROCESSING", "DELIVERED", true);
    }
}
