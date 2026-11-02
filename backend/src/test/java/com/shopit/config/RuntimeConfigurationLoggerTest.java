package com.shopit.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
class RuntimeConfigurationLoggerTest {

    @Test
    void logsNonSecretConfigurationWithoutCredentials(CapturedOutput output) {
        RuntimeConfigurationLogger logger = new RuntimeConfigurationLogger(
                false,
                true,
                false,
                new BigDecimal("49.00"),
                new BigDecimal("1999.00"),
                new BigDecimal("0.18"),
                30);

        logger.run(new DefaultApplicationArguments(new String[0]));

        assertThat(output)
                .contains("apiDocsEnabled=false")
                .contains("rateLimitEnabled=true")
                .contains("simulatedPaymentsEnabled=false")
                .contains("flatShipping=49.00")
                .contains("freeShippingThreshold=1999.00")
                .contains("taxRate=0.18")
                .contains("unpaidOrderTtlMinutes=30")
                .doesNotContain("password")
                .doesNotContain("secret");
    }
}
