package com.shopit.service;
import com.shopit.dto.ApiDtos.*;
import com.shopit.entity.*;
import com.shopit.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orders;
    private final CartItemRepository cart;
    private final UserRepository users;
    private final ProductRepository products;
    private final CouponService coupons;
    private final PricingService pricing;
    private final PaymentRepository payments;
    private final OrderStatusHistoryService statusHistory;
    private final OrderStatusTransitions transitions;
    private final boolean simulatedPaymentsEnabled;

    public OrderService(OrderRepository orders, CartItemRepository cart, UserRepository users,
                        ProductRepository products, CouponService coupons, PricingService pricing,
                        PaymentRepository payments, OrderStatusHistoryService statusHistory,
                        OrderStatusTransitions transitions,
                        @Value("${app.payments.simulated-enabled:false}") boolean simulatedPaymentsEnabled) {
        this.orders = orders;
        this.cart = cart;
        this.users = users;
        this.products = products;
        this.coupons = coupons;
        this.pricing = pricing;
        this.payments = payments;
        this.statusHistory = statusHistory;
        this.transitions = transitions;
        this.simulatedPaymentsEnabled = simulatedPaymentsEnabled;
    }

    public OrderView checkout(String email, CheckoutRequest request) {
        AppUser user = user(email);
        Optional<CustomerOrder> existing = orders.findByUser_IdAndRequestId(user.getId(), request.requestId());
        if (existing.isPresent()) return view(existing.get());

        List<CartItem> lines = cart.findByUser_Id(user.getId());
        if (lines.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your cart is empty");

        // A stable lock order reduces deadlocks when two checkouts contain overlapping products.
        lines.sort(Comparator.comparing(line -> line.getProduct().getId()));

        CustomerOrder order = new CustomerOrder();
        order.setUser(user);
        order.setRequestId(request.requestId());
        order.setStatus("PENDING_PAYMENT");
        order.setShippingAddress(request.shippingAddress().trim());
        BigDecimal subtotal = BigDecimal.ZERO;

        List<String> unavailableProducts = new ArrayList<>();
        for (CartItem line : lines) {
            String productName = line.getProduct().getName();
            Product product = products.findByIdForUpdate(line.getProduct().getId()).orElse(null);
            if (product == null) {
                unavailableProducts.add(productName + " (removed)");
                continue;
            }
            if (!product.isActive()) {
                unavailableProducts.add(product.getName() + " (unavailable)");
                continue;
            }
            if (line.getQuantity() > product.getStock()) {
                unavailableProducts.add(product.getName() + " (only " + product.getStock() + " in stock)");
                continue;
            }

            product.setStock(product.getStock() - line.getQuantity());
            subtotal = subtotal.add(product.getPrice().multiply(BigDecimal.valueOf(line.getQuantity())));

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setUnitPrice(product.getPrice());
            item.setQuantity(line.getQuantity());
            order.addItem(item);
        }

        if (!unavailableProducts.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Unavailable cart products: " + String.join(", ", unavailableProducts));
        }

        BigDecimal discount = BigDecimal.ZERO;
        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            CouponApplication coupon = coupons.redeemForCheckout(request.couponCode(), subtotal);
            discount = coupon.discountAmount();
            order.setCouponId(coupon.id());
            order.setCouponCode(coupon.code());
        }
        PricingService.PriceBreakdown totals = pricing.calculate(subtotal, discount);
        order.setSubtotal(subtotal);
        order.setDiscountAmount(discount);
        order.setShipping(totals.shipping());
        order.setTax(totals.tax());
        order.setTotal(totals.total());

        CustomerOrder saved = orders.save(order);
        statusHistory.record(saved, null, "PENDING_PAYMENT", "CUSTOMER");
        cart.deleteByUser_Id(user.getId());
        return view(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderView> mine(String email) {
        AppUser user = user(email);
        return orders.findByUser_IdOrderByCreatedAtDesc(user.getId()).stream().map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public OrderView getMine(String email, Long id) {
        AppUser user = user(email);
        return view(orders.findByIdAndUser_Id(id, user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found")));
    }

    public OrderView cancel(String email, Long id) {
        AppUser user = user(email);
        CustomerOrder order = orders.findByIdAndUser_IdForUpdate(id, user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only unpaid orders can be cancelled by the customer");
        }
        if (payments.findByOrder_IdAndStatus(id, "SUCCESS").isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Paid orders cannot be cancelled");
        }
        transitions.validateTransition(order.getStatus(), "CANCELLED", false);
        String oldStatus = order.getStatus();
        order.setStatus("CANCELLED");
        statusHistory.record(order, oldStatus, "CANCELLED", "CUSTOMER");
        restoreStock(order);
        coupons.releaseUsage(order.getCouponId());
        return view(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderView> adminList(String status, Pageable pageable) {
        String normalizedStatus = status == null || status.isBlank()
                ? null : status.trim().toUpperCase(Locale.ROOT);
        return orders.searchForAdmin(normalizedStatus, pageable).map(this::view);
    }

    public OrderView updateStatus(Long id, String next) {
        CustomerOrder order = orders.findByIdForUpdate(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        boolean successfulPayment = payments.findByOrder_IdAndStatus(id, "SUCCESS").isPresent();
        if ("CANCELLED".equals(next) && successfulPayment) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Paid orders cannot be cancelled");
        }
        transitions.validateTransition(order.getStatus(), next, successfulPayment);

        if ("CANCELLED".equals(next)) {
            restoreStock(order);
            coupons.releaseUsage(order.getCouponId());
        }

        String oldStatus = order.getStatus();
        order.setStatus(next);
        statusHistory.record(order, oldStatus, next, "ADMIN");
        return view(order);
    }

    private void restoreStock(CustomerOrder order) {
        List<OrderItem> items = order.getItems().stream()
            .filter(item -> item.getProduct() != null)
            .sorted(Comparator.comparing(item -> item.getProduct().getId()))
            .collect(Collectors.toList());
        for (OrderItem item : items) {
            Product product = products.findByIdForUpdate(item.getProduct().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Unable to restore product inventory"));
            product.setStock(product.getStock() + item.getQuantity());
        }
    }

    public OrderView view(CustomerOrder order) {
        List<OrderLine> lines = order.getItems().stream()
            .map(item -> new OrderLine(
                item.getProduct() == null ? null : item.getProduct().getId(),
                item.getProductName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))))
            .toList();
        boolean successfulPayment = payments.findByOrder_IdAndStatus(order.getId(), "SUCCESS").isPresent();
        return new OrderView(order.getId(), order.getStatus(), order.getSubtotal(), order.getDiscountAmount(), order.getCouponCode(),
            order.getShipping(), order.getTax(), order.getTotal(), order.getShippingAddress(), order.getCreatedAt(), lines,
            statusHistory.timeline(order.getId()),
            transitions.allowedNextStatuses(order.getStatus(), successfulPayment), simulatedPaymentsEnabled);
    }

    private AppUser user(String email) {
        return users.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
