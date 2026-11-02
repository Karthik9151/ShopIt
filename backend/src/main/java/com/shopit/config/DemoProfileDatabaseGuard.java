package com.shopit.config;

import java.net.URI;
import java.util.Locale;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.stereotype.Component;

/**
 * Prevents the explicitly local demo profile from connecting to shared or remote databases.
 * Runs during bean-factory initialization, before datasource/Flyway initialization.
 * Production/staging profiles are not affected by this local-development guard.
 */
@Component
@Profile("demo")
public class DemoProfileDatabaseGuard implements BeanFactoryPostProcessor, EnvironmentAware {
    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String datasourceUrl = environment.getProperty("spring.datasource.url");
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
