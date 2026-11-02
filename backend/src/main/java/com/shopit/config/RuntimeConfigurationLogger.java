package com.shopit.config;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Logs only non-secret runtime switches and pricing policy at startup.
 * Database URLs, usernames, credentials, tokens, and administrator identities
 * are intentionally not read by or emitted from this component.
 */
@Component
public class RuntimeConfigurationLogger implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(RuntimeConfigurationLogger.class);

    private final boolean apiDocsEnabled;
    private final boolean rateLimitEnabled;
    private final boolean simulatedPaymentsEnabled;
    private final BigDecimal flatShipping;
    private final BigDecimal freeShippingThreshold;
    private final BigDecimal taxRate;
    private final long unpaidOrderTtlMinutes;

    public RuntimeConfigurationLogger(
            @Value("${app.api-docs.enabled:false}") boolean apiDocsEnabled,
            @Value("${app.rate-limit.enabled:true}") boolean rateLimitEnabled,
            @Value("${app.payments.simulated-enabled:false}") boolean simulatedPaymentsEnabled,
            @Value("${app.pricing.shipping-flat:49.00}") BigDecimal flatShipping,
            @Value("${app.pricing.free-shipping-threshold:1999.00}") BigDecimal freeShippingThreshold,
            @Value("${app.pricing.tax-rate:0.18}") BigDecimal taxRate,
            @Value("${app.orders.unpaid-ttl-minutes:30}") long unpaidOrderTtlMinutes) {
        this.apiDocsEnabled = apiDocsEnabled;
        this.rateLimitEnabled = rateLimitEnabled;
        this.simulatedPaymentsEnabled = simulatedPaymentsEnabled;
        this.flatShipping = flatShipping;
        this.freeShippingThreshold = freeShippingThreshold;
        this.taxRate = taxRate;
        this.unpaidOrderTtlMinutes = unpaidOrderTtlMinutes;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info(
                "ShopIt runtime configuration apiDocsEnabled={} rateLimitEnabled={} "
                        + "simulatedPaymentsEnabled={} flatShipping={} freeShippingThreshold={} "
                        + "taxRate={} unpaidOrderTtlMinutes={}",
                apiDocsEnabled,
                rateLimitEnabled,
                simulatedPaymentsEnabled,
                flatShipping,
                freeShippingThreshold,
                taxRate,
                unpaidOrderTtlMinutes);
    }
}
