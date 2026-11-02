package com.shopit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.shopit.entity.CustomerOrder;
import com.shopit.entity.OrderItem;
import com.shopit.entity.Product;
import com.shopit.repository.OrderRepository;
import com.shopit.repository.PaymentRepository;
import com.shopit.repository.ProductRepository;
import com.shopit.service.CouponService;
import com.shopit.service.OrderStatusHistoryService;
import com.shopit.service.UnpaidOrderExpirationProcessor;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UnpaidOrderExpirationProcessorTest {
    @Mock OrderRepository orders;
    @Mock PaymentRepository payments;
    @Mock ProductRepository products;
    @Mock CouponService coupons;
    @Mock OrderStatusHistoryService history;

    @InjectMocks UnpaidOrderExpirationProcessor processor;

    private static final Instant NOW = Instant.parse("2026-11-02T12:00:00Z");
    private static final Instant CUTOFF = NOW.minus(30, ChronoUnit.MINUTES);

    @Test
    void expiresOrderOlderThanThirtyMinutesAndRestoresStockAndCouponUsage() {
        CustomerOrder order = unpaidOrder(41L, CUTOFF.minusSeconds(60));
        Product product = new Product();
        product.setId(9L);
        product.setStock(2);
        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(3);
        order.addItem(item);

        when(orders.findByIdForUpdate(41L)).thenReturn(Optional.of(order));
        when(payments.findByOrder_IdAndStatus(41L, "SUCCESS")).thenReturn(Optional.empty());
        when(products.findByIdForUpdate(9L)).thenReturn(Optional.of(product));

        assertThat(processor.expireIfUnpaidAndExpired(41L, CUTOFF)).isTrue();

        assertThat(order.getStatus()).isEqualTo("CANCELLED");
        assertThat(product.getStock()).isEqualTo(5);
        verify(coupons).releaseUsage(7L);
        verify(history).record(order, "PENDING_PAYMENT", "CANCELLED", "SYSTEM");
    }

    @Test
    void doesNotExpireAnOrderThatHasNotReachedTheThirtyMinuteCutoff() {
        CustomerOrder order = unpaidOrder(42L, CUTOFF.plusSeconds(1));
        when(orders.findByIdForUpdate(42L)).thenReturn(Optional.of(order));

        assertThat(processor.expireIfUnpaidAndExpired(42L, CUTOFF)).isFalse();

        assertThat(order.getStatus()).isEqualTo("PENDING_PAYMENT");
        verifyNoInteractions(payments, products, coupons, history);
    }

    @Test
    void doesNotExpireAnOrderWithASuccessfulPayment() {
        CustomerOrder order = unpaidOrder(43L, CUTOFF.minusSeconds(60));
        when(orders.findByIdForUpdate(43L)).thenReturn(Optional.of(order));
        when(payments.findByOrder_IdAndStatus(43L, "SUCCESS")).thenReturn(Optional.of(new com.shopit.entity.Payment()));

        assertThat(processor.expireIfUnpaidAndExpired(43L, CUTOFF)).isFalse();

        assertThat(order.getStatus()).isEqualTo("PENDING_PAYMENT");
        verifyNoInteractions(products, coupons, history);
    }

    private CustomerOrder unpaidOrder(Long id, Instant createdAt) {
        CustomerOrder order = new CustomerOrder();
        order.setId(id);
        order.setStatus("PENDING_PAYMENT");
        order.setCreatedAt(createdAt);
        order.setCouponId(7L);
        return order;
    }
}
