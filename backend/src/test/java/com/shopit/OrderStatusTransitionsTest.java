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
    void pendingPaymentAllowsConfirmationOnlyAfterSuccessfulPaymentOrCancellation() {
        assertThat(transitions.allowedNextStatuses("PENDING_PAYMENT", false))
                .containsExactly("CANCELLED");
        assertThat(transitions.allowedNextStatuses("PENDING_PAYMENT", true))
                .containsExactly("CONFIRMED");
        assertThat(transitions.isAllowed("PENDING_PAYMENT", "CONFIRMED")).isTrue();
        assertThat(transitions.isAllowed("PENDING_PAYMENT", "CANCELLED")).isTrue();
    }

    @Test
    void paidOrderMustFollowTheForwardProcessingPath() {
        assertThat(transitions.allowedNextStatuses("CONFIRMED", true)).containsExactly("PROCESSING");
        assertThat(transitions.allowedNextStatuses("PROCESSING", true)).containsExactly("SHIPPED");
        assertThat(transitions.allowedNextStatuses("SHIPPED", true)).containsExactly("DELIVERED");
        assertThat(transitions.allowedNextStatuses("DELIVERED", true)).isEmpty();
    }

    @Test
    void cancelledAndDeliveredAreFinal() {
        assertThat(transitions.allowedNextStatuses("CANCELLED", false)).isEmpty();
        assertThat(transitions.allowedNextStatuses("DELIVERED", true)).isEmpty();
    }

    @Test
    void rejectsEveryRepresentativeIllegalBackwardOrSkippedTransition() {
        List<String[]> illegal = List.of(
                new String[]{"PENDING_PAYMENT", "PROCESSING"},
                new String[]{"PENDING_PAYMENT", "SHIPPED"},
                new String[]{"CONFIRMED", "PENDING_PAYMENT"},
                new String[]{"CONFIRMED", "SHIPPED"},
                new String[]{"PROCESSING", "CONFIRMED"},
                new String[]{"SHIPPED", "PROCESSING"},
                new String[]{"DELIVERED", "CANCELLED"},
                new String[]{"CANCELLED", "CONFIRMED"});

        for (String[] transition : illegal) {
            assertThat(transitions.isAllowed(transition[0], transition[1]))
                    .as("%s -> %s", transition[0], transition[1])
                    .isFalse();
        }
    }

    @Test
    void rejectsConfirmationWithoutSuccessfulPaymentWith409() {
        assertThatThrownBy(() ->
                transitions.validateTransition("PENDING_PAYMENT", "CONFIRMED", false))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void acceptsConfirmationAfterSuccessfulPayment() {
        transitions.validateTransition("PENDING_PAYMENT", "CONFIRMED", true);
    }
}
