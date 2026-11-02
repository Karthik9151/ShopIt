package com.shopit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

import com.shopit.repository.OrderRepository;
import com.shopit.repository.PaymentRepository;
import com.shopit.repository.UserRepository;
import com.shopit.service.OrderStatusHistoryService;
import com.shopit.service.OrderStatusTransitions;
import com.shopit.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PaymentServiceSimulationSwitchTest {
    @Mock PaymentRepository payments;
    @Mock OrderRepository orders;
    @Mock UserRepository users;
    @Mock OrderStatusHistoryService history;
    @Mock OrderStatusTransitions transitions;

    @Test
    void disabledSimulationReturnsNotFoundWithoutReadingOrCreatingPaymentData() {
        PaymentService service = new PaymentService(payments, orders, users, history, transitions, false);

        assertThatThrownBy(() -> service.simulate("customer@example.test", 100L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        verifyNoInteractions(payments, orders, users, history, transitions);
    }
}
