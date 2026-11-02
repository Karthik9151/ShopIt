package com.shopit.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PricingService {
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int MONEY_SCALE = 2;

    private final BigDecimal shippingFlat;
    private final BigDecimal freeShippingThreshold;
    private final BigDecimal taxRate;

    public PricingService(
            @Value("${app.pricing.shipping-flat:49.00}") BigDecimal shippingFlat,
            @Value("${app.pricing.free-shipping-threshold:1999.00}") BigDecimal freeShippingThreshold,
            @Value("${app.pricing.tax-rate:0.18}") BigDecimal taxRate) {
        this.shippingFlat = shippingFlat.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        this.freeShippingThreshold = freeShippingThreshold.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        this.taxRate = taxRate;
    }

    public PriceBreakdown calculate(BigDecimal subtotal, BigDecimal discount) {
        BigDecimal safeSubtotal = money(subtotal);
        BigDecimal safeDiscount = money(discount).max(ZERO).min(safeSubtotal);
        BigDecimal taxableSubtotal = safeSubtotal.subtract(safeDiscount).max(ZERO);
        BigDecimal shipping = taxableSubtotal.compareTo(freeShippingThreshold) >= 0
                ? ZERO.setScale(MONEY_SCALE)
                : shippingFlat;
        BigDecimal tax = taxableSubtotal.multiply(taxRate).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal total = taxableSubtotal.add(shipping).add(tax).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal freeShippingRemaining = freeShippingThreshold.subtract(taxableSubtotal)
                .max(ZERO).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        return new PriceBreakdown(shipping, tax, total, freeShippingRemaining);
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? ZERO : value).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public record PriceBreakdown(
            BigDecimal shipping, BigDecimal tax, BigDecimal total, BigDecimal freeShippingRemaining) {}
}
