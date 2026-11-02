package com.shopit.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DemoProfileDatabaseGuardTest {
    @Test
    void acceptsOnlyLoopbackMysqlDataSources() {
        assertThat(DemoProfileDatabaseGuard.isLocalOnlyDatasource(
                "jdbc:mysql://127.0.0.1:3306/shopit_demo?useSSL=false")).isTrue();
        assertThat(DemoProfileDatabaseGuard.isLocalOnlyDatasource(
                "jdbc:mysql://localhost:3306/shopit_demo")).isTrue();
        assertThat(DemoProfileDatabaseGuard.isLocalOnlyDatasource(
                "jdbc:mysql://[::1]:3306/shopit_demo")).isTrue();

        assertThat(DemoProfileDatabaseGuard.isLocalOnlyDatasource(
                "jdbc:mysql://db.example.com:3306/shopit")).isFalse();
        assertThat(DemoProfileDatabaseGuard.isLocalOnlyDatasource(
                "jdbc:mysql://192.0.2.10:3306/shopit")).isFalse();
        assertThat(DemoProfileDatabaseGuard.isLocalOnlyDatasource(
                "jdbc:h2:mem:shopit")).isFalse();
        assertThat(DemoProfileDatabaseGuard.isLocalOnlyDatasource(null)).isFalse();
    }
}
