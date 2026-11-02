package com.shopit.service;
import com.shopit.dto.ApiDtos.*;
import com.shopit.entity.*;
import com.shopit.repository.*;
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
    private static final BigDecimal SHIPPING = new BigDecimal("49.00");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.18");
    private final OrderRepository orders;
    private final CartItemRepository cart;
    private final UserRepository users;
    private final ProductRepository products;

    public OrderService(OrderRepository orders, CartItemRepository cart, UserRepository users, ProductRepository products) {
        this.orders = orders;
        this.cart = cart;
        this.users = users;
        this.products = products;
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

        for (CartItem line : lines) {
            Product product = products.findByIdForUpdate(line.getProduct().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Product unavailable"));
            if (!product.isActive() || line.getQuantity() > product.getStock()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock changed for " + product.getName());
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

        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        order.setSubtotal(subtotal);
        order.setShipping(SHIPPING);
        order.setTax(tax);
        order.setTotal(subtotal.add(SHIPPING).add(tax).setScale(2, RoundingMode.HALF_UP));

        CustomerOrder saved = orders.save(order);
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
        CustomerOrder order = orders.findByIdAndUser_Id(id, user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only unpaid orders can be cancelled by the customer");
        }
        order.setStatus("CANCELLED");
        restoreStock(order);
        return view(order);
    }

    @Transactional(readOnly = true)
    public List<OrderView> all() {
        return orders.findAllByOrderByCreatedAtDesc().stream().map(this::view).toList();
    }

    public OrderView updateStatus(Long id, String next) {
        Set<String> valid = Set.of("CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED");
        if (!valid.contains(next)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported order status");

        CustomerOrder order = orders.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (Set.of("CANCELLED", "DELIVERED").contains(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order is final");
        }
        if ("CANCELLED".equals(next)) {
            if (!"PENDING_PAYMENT".equals(order.getStatus())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Refund handling is required before cancellation");
            }
            restoreStock(order);
        }
        order.setStatus(next);
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
        return new OrderView(order.getId(), order.getStatus(), order.getSubtotal(), order.getShipping(),
            order.getTax(), order.getTotal(), order.getShippingAddress(), order.getCreatedAt(), lines);
    }

    private AppUser user(String email) {
        return users.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
