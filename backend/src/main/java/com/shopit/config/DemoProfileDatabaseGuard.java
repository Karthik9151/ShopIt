package com.shopit.config;

import java.net.URI;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Prevents the explicitly local demo profile from connecting to shared or remote databases.
 * Production/staging profiles are not affected by this local-development guard.
 */
@Component
@Profile("demo")
public class DemoProfileDatabaseGuard implements ApplicationRunner {
    private final String datasourceUrl;

    public DemoProfileDatabaseGuard(@Value("${spring.datasource.url}") String datasourceUrl) {
        this.datasourceUrl = datasourceUrl;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!isLocalOnlyDatasource(datasourceUrl)) {
            throw new IllegalStateException(
                    "The demo profile may connect only to a loopback MySQL database. "
                            + "Use a separate profile for shared or hosted databases.");
        }
    }

    static boolean isLocalOnlyDatasource(String jdbcUrl) {
        if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:mysql://")) {
            return false;
        }

        try {
            URI uri = URI.create(jdbcUrl.substring("jdbc:".length()));
            String host = uri.getHost();
            if (host == null) {
                return false;
            }

            String normalizedHost = host.replace("[", "").replace("]", "")
                    .toLowerCase(Locale.ROOT);
            return normalizedHost.equals("localhost")
                    || normalizedHost.equals("127.0.0.1")
                    || normalizedHost.equals("::1");
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
