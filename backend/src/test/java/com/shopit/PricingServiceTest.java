package com.shopit;

import static org.assertj.core.api.Assertions.assertThat;

import com.shopit.service.PricingService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PricingServiceTest {
    private final PricingService pricing = new PricingService(
            new BigDecimal("49.00"), new BigDecimal("1999.00"), new BigDecimal("0.18"));

    @Test
    void subtotalBelowThresholdPaysFlatShipping() {
        var result = pricing.calculate(new BigDecimal("1998.99"), BigDecimal.ZERO);
        assertThat(result.shipping()).isEqualByComparingTo("49.00");
        assertThat(result.tax()).isEqualByComparingTo("359.82");
        assertThat(result.total()).isEqualByComparingTo("2407.81");
        assertThat(result.freeShippingRemaining()).isEqualByComparingTo("0.01");
    }

    @Test
    void subtotalAtThresholdGetsFreeShipping() {
        var result = pricing.calculate(new BigDecimal("1999.00"), BigDecimal.ZERO);
        assertThat(result.shipping()).isEqualByComparingTo("0.00");
        assertThat(result.tax()).isEqualByComparingTo("359.82");
        assertThat(result.total()).isEqualByComparingTo("2358.82");
        assertThat(result.freeShippingRemaining()).isEqualByComparingTo("0.00");
    }

    @Test
    void couponDiscountCanRestoreShippingWhenDiscountedSubtotalFallsBelowThreshold() {
        var result = pricing.calculate(new BigDecimal("2000.00"), new BigDecimal("1.01"));
        assertThat(result.shipping()).isEqualByComparingTo("49.00");
        assertThat(result.tax()).isEqualByComparingTo("359.82");
        assertThat(result.total()).isEqualByComparingTo("2407.81");
        assertThat(result.freeShippingRemaining()).isEqualByComparingTo("0.01");
    }

    @Test
    void taxRoundsHalfUpToTwoDecimalPlaces() {
        var result = pricing.calculate(new BigDecimal("100.05"), BigDecimal.ZERO);
        assertThat(result.tax()).isEqualByComparingTo("18.01");
        assertThat(result.total()).isEqualByComparingTo("167.06");
    }

    @Test
    void discountCannotReduceTaxableSubtotalBelowZero() {
        var result = pricing.calculate(new BigDecimal("10.00"), new BigDecimal("15.00"));
        assertThat(result.shipping()).isEqualByComparingTo("49.00");
        assertThat(result.tax()).isEqualByComparingTo("0.00");
        assertThat(result.total()).isEqualByComparingTo("49.00");
    }
}
