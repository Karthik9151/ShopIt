package com.shopit.service;

import com.shopit.entity.CustomerOrder;
import com.shopit.entity.OrderItem;
import com.shopit.entity.Product;
import com.shopit.repository.OrderRepository;
import com.shopit.repository.PaymentRepository;
import com.shopit.repository.ProductRepository;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UnpaidOrderExpirationProcessor {
    private final OrderRepository orders;
    private final PaymentRepository payments;
    private final ProductRepository products;
    private final CouponService coupons;
    private final OrderStatusHistoryService statusHistory;

    public UnpaidOrderExpirationProcessor(
            OrderRepository orders,
            PaymentRepository payments,
            ProductRepository products,
            CouponService coupons,
            OrderStatusHistoryService statusHistory) {
        this.orders = orders;
        this.payments = payments;
        this.products = products;
        this.coupons = coupons;
        this.statusHistory = statusHistory;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean expireIfUnpaidAndExpired(Long orderId, Instant cutoff) {
        CustomerOrder order = orders.findByIdForUpdate(orderId).orElse(null);
        if (order == null
                || !"PENDING_PAYMENT".equals(order.getStatus())
                || order.getCreatedAt().isAfter(cutoff)
                || payments.findByOrder_IdAndStatus(orderId, "SUCCESS").isPresent()) {
            return false;
        }

        restoreStock(order);
        coupons.releaseUsage(order.getCouponId());

        String oldStatus = order.getStatus();
        order.setStatus("CANCELLED");
        statusHistory.record(order, oldStatus, "CANCELLED", "SYSTEM");
        return true;
    }

    private void restoreStock(CustomerOrder order) {
        List<OrderItem> items = order.getItems().stream()
                .filter(item -> item.getProduct() != null)
                .sorted(Comparator.comparing(item -> item.getProduct().getId()))
                .toList();

        for (OrderItem item : items) {
            Product product = products.findByIdForUpdate(item.getProduct().getId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Unable to restore inventory for expired order " + order.getId()));
            product.setStock(product.getStock() + item.getQuantity());
        }
    }
}
