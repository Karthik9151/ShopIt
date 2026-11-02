package com.shopit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("ci")
class CouponAndInventoryIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private record Customer(String token) {}

    private Customer registerCustomer() throws Exception {
        String email = "coupon-" + UUID.randomUUID() + "@shopit.test";
        String body = mapper.createObjectNode()
            .put("name", "Coupon Customer")
            .put("email", email)
            .put("password", "Customer-Password-2026!")
            .toString();
        MvcResult result = mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated()).andReturn();
        return new Customer(mapper.readTree(result.getResponse().getContentAsString()).get("token").asText());
    }

    private String adminToken() throws Exception {
        String body = mapper.createObjectNode()
            .put("email", "admin@shopit.test")
            .put("password", "ShopIt-Admin-Test-2026!")
            .toString();
        MvcResult result = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk()).andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void onlyOwnerCanSetCostAndTimeLimitedCouponIsAppliedAtCheckoutOnce() throws Exception {
        String admin = adminToken();
        String productName = "Coupon Test Product " + UUID.randomUUID();
        String productBody = mapper.createObjectNode()
            .put("name", productName)
            .put("description", "Private cost-price and coupon integration test")
            .put("price", 100.00)
            .put("costPrice", 40.00)
            .put("stock", 5)
            .put("active", true)
            .toString();

        MvcResult productResult = mvc.perform(post("/api/admin/products")
                .header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON).content(productBody))
            .andExpect(status().isCreated()).andReturn();
        JsonNode adminProduct = mapper.readTree(productResult.getResponse().getContentAsString());
        long productId = adminProduct.get("id").asLong();
        assertThat(adminProduct.get("costPrice").decimalValue()).isEqualByComparingTo("40.00");

        mvc.perform(get("/api/products/" + productId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.costPrice").doesNotExist());

        MvcResult adminProducts = mvc.perform(get("/api/admin/products")
                .header("Authorization", "Bearer " + admin))
            .andExpect(status().isOk()).andReturn();
        JsonNode adminCatalogue = mapper.readTree(adminProducts.getResponse().getContentAsString());
        boolean privateCostVisibleToAdmin = false;
        for (JsonNode product : adminCatalogue) {
            if (product.get("id").asLong() == productId) {
                privateCostVisibleToAdmin = product.get("costPrice").decimalValue().compareTo(new BigDecimal("40.00")) == 0;
            }
        }
        assertThat(privateCostVisibleToAdmin).isTrue();

        mvc.perform(get("/api/admin/coupons"))
            .andExpect(status().isUnauthorized());

        String code = "SH" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        String couponBody = mapper.createObjectNode()
            .put("code", code)
            .put("description", "10 percent off, single redemption")
            .put("discountType", "PERCENT")
            .put("discountValue", 10.00)
            .put("minimumOrderAmount", 0.00)
            .put("startsAt", Instant.now().minusSeconds(60).toString())
            .put("endsAt", Instant.now().plusSeconds(3600).toString())
            .put("usageLimit", 1)
            .put("active", true)
            .toString();

        mvc.perform(post("/api/admin/coupons")
                .header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON).content(couponBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value(code))
            .andExpect(jsonPath("$.available").value(true));

        mvc.perform(get("/api/coupons"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.code == '" + code + "')]").isNotEmpty());

        Customer first = registerCustomer();
        mvc.perform(post("/api/cart/items")
                .header("Authorization", "Bearer " + first.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.createObjectNode().put("productId", productId).put("quantity", 1).toString()))
            .andExpect(status().isOk());

        mvc.perform(post("/api/coupons/validate")
                .header("Authorization", "Bearer " + first.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.createObjectNode().put("code", code).toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.discountAmount").value(10.0));

        String orderRequest = mapper.createObjectNode()
            .put("shippingAddress", "12 Test Street, Hyderabad, Telangana, 500001")
            .put("requestId", UUID.randomUUID().toString())
            .put("couponCode", code)
            .toString();

        MvcResult placed = mvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + first.token())
                .contentType(MediaType.APPLICATION_JSON).content(orderRequest))
            .andExpect(status().isCreated()).andReturn();
        JsonNode order = mapper.readTree(placed.getResponse().getContentAsString());
        assertThat(order.get("couponCode").asText()).isEqualTo(code);
        assertThat(order.get("subtotal").decimalValue()).isEqualByComparingTo("100.00");
        assertThat(order.get("discountAmount").decimalValue()).isEqualByComparingTo("10.00");
        assertThat(order.get("total").decimalValue()).isEqualByComparingTo("155.20");

        Customer second = registerCustomer();
        mvc.perform(post("/api/cart/items")
                .header("Authorization", "Bearer " + second.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.createObjectNode().put("productId", productId).put("quantity", 1).toString()))
            .andExpect(status().isOk());

        mvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + second.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.createObjectNode()
                    .put("shippingAddress", "12 Test Street, Hyderabad, Telangana, 500001")
                    .put("requestId", UUID.randomUUID().toString())
                    .put("couponCode", code)
                    .toString()))
            .andExpect(status().isBadRequest());

        // Cancelling the unpaid order releases both stock and the coupon's single redemption slot.
        mvc.perform(post("/api/orders/" + order.get("id").asLong() + "/cancel")
                .header("Authorization", "Bearer " + first.token()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"));

        Customer third = registerCustomer();
        mvc.perform(post("/api/cart/items")
                .header("Authorization", "Bearer " + third.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.createObjectNode().put("productId", productId).put("quantity", 1).toString()))
            .andExpect(status().isOk());

        mvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + third.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.createObjectNode()
                    .put("shippingAddress", "12 Test Street, Hyderabad, Telangana, 500001")
                    .put("requestId", UUID.randomUUID().toString())
                    .put("couponCode", code)
                    .toString()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.discountAmount").value(10.0));
    }
}
