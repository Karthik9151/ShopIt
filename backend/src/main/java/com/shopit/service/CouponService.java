package com.shopit.service;

import com.shopit.dto.ApiDtos.*;
import com.shopit.entity.*;
import com.shopit.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.*;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class CouponService {
    private static final Pattern CODE_PATTERN = Pattern.compile("[A-Z0-9_-]{3,40}");
    private final CouponRepository coupons;
    private final CartItemRepository cart;
    private final UserRepository users;
    private final PricingService pricing;

    public CouponService(CouponRepository coupons, CartItemRepository cart, UserRepository users, PricingService pricing) {
        this.coupons = coupons;
        this.cart = cart;
        this.users = users;
        this.pricing = pricing;
    }

    @Transactional(readOnly = true)
    public List<CouponView> activeOffers() {
        Instant now = Instant.now();
        return coupons.findByActiveTrueOrderByEndsAtAsc().stream()
            .filter(c -> isWithinWindow(c, now) && hasUsesRemaining(c)).map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public List<CouponView> allForAdmin() {
        return coupons.findAllByOrderByCreatedAtDesc().stream().map(this::view).toList();
    }

    @Transactional
    public CouponView create(CouponRequest request) {
        validateRequest(request, null);
        Coupon coupon = new Coupon();
        apply(coupon, request);
        return view(coupons.save(coupon));
    }

    @Transactional
    public CouponView update(Long id, CouponRequest request) {
        Coupon coupon = coupons.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Coupon not found"));
        validateRequest(request, id);
        apply(coupon, request);
        return view(coupons.save(coupon));
    }

    @Transactional
    public CouponView setActive(Long id, boolean active) {
        Coupon coupon = coupons.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Coupon not found"));
        coupon.setActive(active);
        return view(coupon);
    }

    @Transactional(readOnly = true)
    public CouponPreview preview(String email, String rawCode) {
        String code = normalizeCode(rawCode);
        AppUser user = user(email);
        List<CartItem> lines = cart.findByUser_Id(user.getId());
        if (lines.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your cart is empty");
        BigDecimal subtotal = lines.stream()
            .map(line -> line.getProduct().getPrice().multiply(BigDecimal.valueOf(line.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        Coupon coupon = coupons.findByCodeIgnoreCase(code).orElseThrow(this::invalidCoupon);
        BigDecimal discount = validateAndCalculate(coupon, subtotal, Instant.now());
        PricingService.PriceBreakdown totals = pricing.calculate(subtotal, discount);
        return new CouponPreview(coupon.getCode(), coupon.getDescription(), subtotal, discount,
            totals.shipping(), totals.tax(), totals.total(), coupon.getStartsAt(), coupon.getEndsAt());
    }

    @Transactional
    public CouponApplication redeemForCheckout(String rawCode, BigDecimal subtotal) {
        String code = normalizeCode(rawCode);
        Coupon coupon = coupons.findByCodeIgnoreCaseForUpdate(code).orElseThrow(this::invalidCoupon);
        BigDecimal discount = validateAndCalculate(coupon, subtotal, Instant.now());
        coupon.setUsageCount(coupon.getUsageCount() + 1);
        return new CouponApplication(coupon.getId(), coupon.getCode(), discount);
    }

    @Transactional
    public void releaseUsage(Long couponId) {
        if (couponId == null) return;
        coupons.findByIdForUpdate(couponId).ifPresent(coupon -> {
            if (coupon.getUsageCount() > 0) coupon.setUsageCount(coupon.getUsageCount() - 1);
        });
    }

    private BigDecimal validateAndCalculate(Coupon coupon, BigDecimal subtotal, Instant now) {
        if (!coupon.isActive() || !isWithinWindow(coupon, now) || !hasUsesRemaining(coupon)) {
            throw invalidCoupon();
        }
        BigDecimal minimum = valueOrZero(coupon.getMinimumOrderAmount());
        if (subtotal.compareTo(minimum) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "This coupon requires a minimum cart value of ₹" + minimum);
        }
        BigDecimal discount;
        if ("PERCENT".equals(coupon.getDiscountType())) {
            discount = subtotal.multiply(coupon.getDiscountValue()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else {
            discount = coupon.getDiscountValue().min(subtotal);
        }
        if (coupon.getMaximumDiscount() != null) discount = discount.min(coupon.getMaximumDiscount());
        return discount.max(BigDecimal.ZERO).min(subtotal).setScale(2, RoundingMode.HALF_UP);
    }

    private void validateRequest(CouponRequest request, Long currentId) {
        String code = normalizeCode(request.code());
        if (!CODE_PATTERN.matcher(code).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coupon code must contain 3–40 letters, numbers, hyphens or underscores");
        }
        coupons.findByCodeIgnoreCase(code).filter(existing -> !Objects.equals(existing.getId(), currentId))
            .ifPresent(existing -> { throw new ResponseStatusException(HttpStatus.CONFLICT, "That coupon code already exists"); });
        if (request.startsAt() == null || request.endsAt() == null || !request.endsAt().isAfter(request.startsAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coupon end time must be after its start time");
        }
        if (request.discountType() == null ||
            !Set.of("PERCENT", "FIXED").contains(request.discountType().toUpperCase(Locale.ROOT))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Discount type must be PERCENT or FIXED");
        }
        if (request.discountValue() == null || request.discountValue().signum() <= 0 ||
            ("PERCENT".equalsIgnoreCase(request.discountType()) && request.discountValue().compareTo(new BigDecimal("100")) > 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Discount value is outside the allowed range");
        }
        if (request.minimumOrderAmount() != null && request.minimumOrderAmount().signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimum order amount cannot be negative");
        }
        if (request.maximumDiscount() != null && request.maximumDiscount().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum discount must be greater than zero");
        }
        if (request.usageLimit() != null && request.usageLimit() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usage limit must be at least one");
        }
    }

    private void apply(Coupon coupon, CouponRequest request) {
        coupon.setCode(normalizeCode(request.code()));
        coupon.setDescription(request.description());
        coupon.setDiscountType(request.discountType().toUpperCase(Locale.ROOT));
        coupon.setDiscountValue(request.discountValue());
        coupon.setMinimumOrderAmount(valueOrZero(request.minimumOrderAmount()));
        coupon.setMaximumDiscount(request.maximumDiscount());
        coupon.setStartsAt(request.startsAt()); coupon.setEndsAt(request.endsAt());
        coupon.setUsageLimit(request.usageLimit());
        if (request.active() != null) coupon.setActive(request.active());
    }

    private CouponView view(Coupon c) {
        Instant now = Instant.now();
        return new CouponView(c.getId(), c.getCode(), c.getDescription(), c.getDiscountType(), c.getDiscountValue(),
            c.getMinimumOrderAmount(), c.getMaximumDiscount(), c.getStartsAt(), c.getEndsAt(),
            c.getUsageLimit(), c.getUsageCount(), c.isActive(), isWithinWindow(c, now) && hasUsesRemaining(c));
    }
    private boolean isWithinWindow(Coupon c, Instant now) {
        return c.getStartsAt() != null && c.getEndsAt() != null && !now.isBefore(c.getStartsAt()) && now.isBefore(c.getEndsAt());
    }
    private boolean hasUsesRemaining(Coupon c) { return c.getUsageLimit() == null || c.getUsageCount() < c.getUsageLimit(); }
    private String normalizeCode(String raw) {
        if (raw == null || raw.isBlank()) throw invalidCoupon();
        return raw.trim().toUpperCase(Locale.ROOT);
    }
    private BigDecimal valueOrZero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    private AppUser user(String email) {
        return users.findByEmailIgnoreCase(email).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in to apply a coupon"));
    }
    private ResponseStatusException invalidCoupon() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "This coupon code is invalid, expired, inactive or fully used");
    }
}
