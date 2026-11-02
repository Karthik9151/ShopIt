package com.shopit.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class ApiDtos {
    private ApiDtos() {}

    public record RegisterRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Email @Size(max = 190) String email,
            @NotBlank @Size(min = 10, max = 72) String password,
            @Size(max = 30) String phone) {}

    public record LoginRequest(
            @NotBlank @Email @Size(max = 190) String email,
            @NotBlank @Size(max = 72) String password) {}

    public record UserView(Long id, String name, String email, String role, String phone) {}
    public record AuthResponse(String token, UserView user, long expiresInMs) {}
    public record CategoryView(Long id, String name, String slug, String description) {}

    public record AdminCategoryView(Long id, String name, String slug, String description, boolean active, long productCount) {}

    public record CategoryRequest(
            @NotBlank @Size(max = 100) String name,
            @Size(max = 500) String description) {}

    public record ProductRequest(
            @NotBlank @Size(max = 180) String name,
            @Size(max = 10000) String description,
            @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal price,
            @Min(0) @Max(1000000) int stock,
            @Size(max = 1000) String imageUrl,
            Long categoryId,
            Boolean active,
            @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal costPrice) {}

    public record ProductView(
            Long id,
            String name,
            String description,
            BigDecimal price,
            boolean inStock,
            boolean lowStock,
            String imageUrl,
            Long categoryId,
            String categoryName,
            boolean active,
            Instant createdAt) {}

    public record AdminProductView(
            Long id,
            String name,
            String description,
            BigDecimal price,
            BigDecimal costPrice,
            int stock,
            String imageUrl,
            Long categoryId,
            String categoryName,
            boolean active,
            Instant createdAt) {}

    public record CartAddRequest(@NotNull Long productId, @Min(1) @Max(100) int quantity) {}
    public record CartUpdateRequest(@Min(1) @Max(100) int quantity) {}

    public record CartLine(
            Long id,
            Long productId,
            String name,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal,
            String imageUrl,
            int availableStock,
            boolean available,
            String unavailableReason) {}

    public record CartView(
            List<CartLine> items,
            BigDecimal subtotal,
            int itemCount,
            BigDecimal shipping,
            BigDecimal tax,
            BigDecimal total,
            BigDecimal freeShippingRemaining) {}

    public record WishlistLine(
            Long id,
            Long productId,
            String name,
            BigDecimal price,
            String imageUrl,
            boolean available,
            String unavailableReason) {}

    public record CheckoutRequest(
            @NotBlank @Size(max = 1000) String shippingAddress,
            @NotBlank @Size(max = 80) String requestId,
            @Size(max = 40) String couponCode) {}

    public record OrderLine(Long productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {}
    public record OrderStatusHistoryView(String oldStatus, String newStatus, Instant changedAt, String actor) {}

    public record OrderView(
            Long id,
            String status,
            BigDecimal subtotal,
            BigDecimal discountAmount,
            String couponCode,
            BigDecimal shipping,
            BigDecimal tax,
            BigDecimal total,
            String shippingAddress,
            Instant createdAt,
            List<OrderLine> items,
            List<OrderStatusHistoryView> timeline,
            List<String> allowedNextStatuses,
            boolean simulatedPaymentEnabled) {}

    public record PaymentView(
            Long id, Long orderId, String provider, String status, String reference, BigDecimal amount, Instant paidAt) {}

    public record StatusUpdate(@NotBlank @Size(max = 30) String status) {}

    public record StockUpdateRequest(@Min(0) @Max(1000000) int stock) {}
    public record ProfileUpdateRequest(@NotBlank @Size(max = 120) String name, @Size(max = 30) String phone) {}

    public record PasswordChangeRequest(
            @NotBlank @Size(max = 72) String currentPassword,
            @NotBlank @Size(min = 10, max = 72) String newPassword) {}

    public record AddressRequest(
            @NotBlank @Size(max = 60) String label,
            @NotBlank @Size(max = 120) String recipientName,
            @NotBlank @Size(max = 30) String phone,
            @NotBlank @Size(max = 240) String addressLine1,
            @Size(max = 240) String addressLine2,
            @NotBlank @Size(max = 100) String city,
            @NotBlank @Size(max = 100) String state,
            @NotBlank @Size(max = 20) String postalCode,
            @NotBlank @Size(max = 80) String country,
            boolean defaultAddress) {}

    public record AddressView(
            Long id,
            String label,
            String recipientName,
            String phone,
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            String postalCode,
            String country,
            boolean defaultAddress) {}

    public record CustomerStatusRequest(@NotNull Boolean enabled) {}

    public record CategoryStatusRequest(@NotNull Boolean active) {}

    public record CustomerView(Long id, String name, String email, String phone, boolean enabled, Instant createdAt) {}

    public record DashboardView(
            long customers,
            long products,
            long orders,
            long paidPayments,
            BigDecimal verifiedSales,
            BigDecimal simulatedPaymentValue,
            long pendingOrders,
            long lowStockProducts,
            BigDecimal inventoryCostBudget,
            BigDecimal potentialRetailValue,
            BigDecimal estimatedGrossMargin) {}

    public record CouponRequest(
            @NotBlank @Size(max = 40) String code,
            @Size(max = 240) String description,
            @NotBlank String discountType,
            @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal discountValue,
            @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal minimumOrderAmount,
            @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal maximumDiscount,
            @NotNull Instant startsAt,
            @NotNull Instant endsAt,
            @Min(1) Integer usageLimit,
            Boolean active) {}

    public record CouponView(
            Long id,
            String code,
            String description,
            String discountType,
            BigDecimal discountValue,
            BigDecimal minimumOrderAmount,
            BigDecimal maximumDiscount,
            Instant startsAt,
            Instant endsAt,
            Integer usageLimit,
            int usageCount,
            boolean active,
            boolean available) {}

    public record CouponCheckRequest(@NotBlank @Size(max = 40) String code) {}

    public record CouponStatusRequest(@NotNull Boolean active) {}

    public record CouponPreview(
            String code,
            String description,
            BigDecimal subtotal,
            BigDecimal discountAmount,
            BigDecimal shipping,
            BigDecimal tax,
            BigDecimal total,
            Instant startsAt,
            Instant endsAt) {}

    public record CouponApplication(Long id, String code, BigDecimal discountAmount) {}
}
