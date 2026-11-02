package com.shopit.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Calculates server-authoritative checkout totals.
 *
 * Shipping eligibility is evaluated after the coupon discount, and tax is
 * calculated from that same discounted subtotal. All monetary outputs are
 * rounded to two decimal places using HALF_UP.
 */
@Service
public class PricingService {
    private static final BigDecimal ZERO_AMOUNT = BigDecimal.ZERO;
    private static final int MONEY_SCALE = 2;
    private static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    private final BigDecimal flatShippingAmount;
    private final BigDecimal freeShippingThreshold;
    private final BigDecimal taxRate;

    public PricingService(
            @Value("${app.pricing.shipping-flat:49.00}") BigDecimal flatShippingAmount,
            @Value("${app.pricing.free-shipping-threshold:1999.00}") BigDecimal freeShippingThreshold,
            @Value("${app.pricing.tax-rate:0.18}") BigDecimal taxRate) {
        this.flatShippingAmount = normalizeMoney(flatShippingAmount);
        this.freeShippingThreshold = normalizeMoney(freeShippingThreshold);
        this.taxRate = taxRate;
    }

    public PriceBreakdown calculate(BigDecimal subtotal, BigDecimal discount) {
        BigDecimal normalizedSubtotal = normalizeMoney(subtotal);
        BigDecimal appliedDiscount = normalizeMoney(discount)
                .max(ZERO_AMOUNT)
                .min(normalizedSubtotal);
        BigDecimal taxableSubtotal = normalizedSubtotal
                .subtract(appliedDiscount)
                .max(ZERO_AMOUNT);

        BigDecimal shipping = calculateShipping(taxableSubtotal);
        BigDecimal tax = calculateTax(taxableSubtotal);
        BigDecimal total = taxableSubtotal
                .add(shipping)
                .add(tax)
                .setScale(MONEY_SCALE, MONEY_ROUNDING);
        BigDecimal remainingForFreeShipping = calculateFreeShippingRemaining(taxableSubtotal);

        return new PriceBreakdown(shipping, tax, total, remainingForFreeShipping);
    }

    private BigDecimal calculateShipping(BigDecimal discountedSubtotal) {
        if (discountedSubtotal.compareTo(freeShippingThreshold) >= 0) {
            return ZERO_AMOUNT.setScale(MONEY_SCALE, MONEY_ROUNDING);
        }
        return flatShippingAmount;
    }

    private BigDecimal calculateTax(BigDecimal discountedSubtotal) {
        return discountedSubtotal
                .multiply(taxRate)
                .setScale(MONEY_SCALE, MONEY_ROUNDING);
    }

    private BigDecimal calculateFreeShippingRemaining(BigDecimal discountedSubtotal) {
        return freeShippingThreshold
                .subtract(discountedSubtotal)
                .max(ZERO_AMOUNT)
                .setScale(MONEY_SCALE, MONEY_ROUNDING);
    }

    private static BigDecimal normalizeMoney(BigDecimal amount) {
        BigDecimal safeAmount = amount == null ? ZERO_AMOUNT : amount;
        return safeAmount.setScale(MONEY_SCALE, MONEY_ROUNDING);
    }

    public record PriceBreakdown(
            BigDecimal shipping,
            BigDecimal tax,
            BigDecimal total,
            BigDecimal freeShippingRemaining) {
    }
}
