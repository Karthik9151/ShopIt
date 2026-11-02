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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("ci")
class CommerceFlowIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private record Customer(String token, long id, String email) {}

    private Customer register() throws Exception {
        String email = "customer-" + UUID.randomUUID() + "@shopit.test";
        String body = mapper.createObjectNode()
            .put("name", "Integration Customer")
            .put("email", email)
            .put("password", "Customer-Password-2026!")
            .put("phone", "9876543210")
            .toString();
        MvcResult result = mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated()).andReturn();
        JsonNode json = mapper.readTree(result.getResponse().getContentAsString());
        return new Customer(json.get("token").asText(), json.get("user").get("id").asLong(), email);
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
    void customerResourcesAreIsolatedAndAdminRoutesAreProtected() throws Exception {
        Customer owner = register();
        Customer other = register();
        String ownerAddress = mapper.createObjectNode()
            .put("label", "Home").put("recipientName", "Integration Customer")
            .put("phone", "9876543210").put("addressLine1", "12 Test Street")
            .put("addressLine2", "").put("city", "Hyderabad").put("state", "Telangana")
            .put("postalCode", "500001").put("country", "India").put("defaultAddress", true).toString();

        MvcResult created = mvc.perform(post("/api/addresses")
                .header("Authorization", "Bearer " + owner.token())
                .contentType(MediaType.APPLICATION_JSON).content(ownerAddress))
            .andExpect(status().isCreated()).andReturn();
        long addressId = mapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(delete("/api/addresses/" + addressId)
                .header("Authorization", "Bearer " + other.token()))
            .andExpect(status().isNotFound());
        mvc.perform(get("/api/addresses")
                .header("Authorization", "Bearer " + owner.token()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/admin/dashboard")
                .header("Authorization", "Bearer " + owner.token()))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/dashboard"))
            .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/dashboard")
                .header("Authorization", "Bearer " + adminToken()))
            .andExpect(status().isOk());
    }

    @Test
    void checkoutAndSimulationDoNotInflateVerifiedSales() throws Exception {
        Customer customer = register();
        Customer other = register();

        MvcResult catalogue = mvc.perform(get("/api/products?size=1"))
            .andExpect(status().isOk()).andReturn();
        JsonNode productPage = mapper.readTree(catalogue.getResponse().getContentAsString());
        assertThat(productPage.get("content").size()).isGreaterThan(0);
        long productId = productPage.get("content").get(0).get("id").asLong();

        mvc.perform(post("/api/cart/items")
                .header("Authorization", "Bearer " + customer.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.createObjectNode().put("productId", productId).put("quantity", 1).toString()))
            .andExpect(status().isOk());

        String requestId = UUID.randomUUID().toString();
        String checkout = mapper.createObjectNode()
            .put("shippingAddress", "12 Test Street, Hyderabad, Telangana, 500001")
            .put("requestId", requestId).toString();
        MvcResult placed = mvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + customer.token())
                .contentType(MediaType.APPLICATION_JSON).content(checkout))
            .andExpect(status().isCreated()).andReturn();
        JsonNode order = mapper.readTree(placed.getResponse().getContentAsString());
        long orderId = order.get("id").asLong();
        assertThat(order.get("status").asText()).isEqualTo("PENDING_PAYMENT");

        mvc.perform(get("/api/orders/" + orderId)
                .header("Authorization", "Bearer " + other.token()))
            .andExpect(status().isNotFound());

        MvcResult paid = mvc.perform(post("/api/payments/simulate/" + orderId)
                .header("Authorization", "Bearer " + customer.token()))
            .andExpect(status().isOk()).andReturn();
        JsonNode payment = mapper.readTree(paid.getResponse().getContentAsString());
        assertThat(payment.get("provider").asText()).isEqualTo("SIMULATED");
        assertThat(payment.get("status").asText()).isEqualTo("SUCCESS");

        // A retry with the same idempotency key returns the original order after the cart is cleared.
        MvcResult retry = mvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + customer.token())
                .contentType(MediaType.APPLICATION_JSON).content(checkout))
            .andExpect(status().isCreated()).andReturn();
        assertThat(mapper.readTree(retry.getResponse().getContentAsString()).get("id").asLong()).isEqualTo(orderId);

        MvcResult dashboard = mvc.perform(get("/api/admin/dashboard")
                .header("Authorization", "Bearer " + adminToken()))
            .andExpect(status().isOk()).andReturn();
        JsonNode metrics = mapper.readTree(dashboard.getResponse().getContentAsString());
        assertThat(metrics.get("verifiedSales").decimalValue()).isEqualByComparingTo("0.00");
        assertThat(metrics.get("simulatedPaymentValue").decimalValue()).isGreaterThanOrEqualTo(payment.get("amount").decimalValue());
    }


    @Test
    void catalogueSupportsAdminBatchSizeWithinConfiguredLimit() throws Exception {
        mvc.perform(get("/api/products?size=100"))
            .andExpect(status().isOk());

        mvc.perform(get("/api/products?size=101"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void administratorCanDisableCustomerAndExistingJwtStopsAuthenticating() throws Exception {
        Customer customer = register();
        String admin = adminToken();
        mvc.perform(put("/api/admin/customers/" + customer.id() + "/status")
                .header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.createObjectNode().put("enabled", false).toString()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.enabled").value(false));

        mvc.perform(get("/api/profile").header("Authorization", "Bearer " + customer.token()))
            .andExpect(status().isUnauthorized());
    }
    @Test
    void configuredAdministratorEmailIsReservedAndAdminLoginReturnsAdminRole() throws Exception {
        String registration = mapper.createObjectNode()
            .put("name", "Reserved Email")
            .put("email", ADMIN@SHOPIT.TEST)
            .put("password", "Customer-Password-2026!")
            .toString();

        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON).content(registration))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("An account with this email already exists"));

        String login = mapper.createObjectNode()
            .put("email", " ADMIN@SHOPIT.TEST ")
            .put("password", "ShopIt-Admin-Test-2026!")
            .toString();

        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON).content(login))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }

}
