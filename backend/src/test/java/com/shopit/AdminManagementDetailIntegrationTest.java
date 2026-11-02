package com.shopit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopit.entity.AppUser;
import com.shopit.entity.CustomerOrder;
import com.shopit.repository.OrderRepository;
import com.shopit.repository.UserRepository;
import com.shopit.support.TestAdminCredentials;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("ci")
class AdminManagementDetailIntegrationTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    UserRepository users;

    @Autowired
    OrderRepository orders;

    @Test
    void productDetailAndActivationEndpointsAreAdminOnly() throws Exception {
        String adminToken = login(TestAdminCredentials.email(), TestAdminCredentials.password());
        String customerToken = registerCustomer();

        String suffix = UUID.randomUUID().toString().replace("-", "");
        String body = mapper.createObjectNode()
                .put("name", "Admin detail " + suffix)
                .put("description", "A product created by an integration test")
                .put("price", 125.50)
                .put("costPrice", 50.25)
                .put("stock", 9)
                .put("active", true)
                .toString();

        MvcResult created = mvc.perform(post("/api/admin/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.costPrice").value(50.25))
                .andReturn();
        long productId = mapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(get("/api/admin/products/" + productId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.costPrice").value(50.25));

        mvc.perform(patch("/api/admin/products/" + productId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mvc.perform(get("/api/admin/products/" + productId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());

        mvc.perform(patch("/api/admin/products/" + productId + "/status")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":true}"))
                .andExpect(status().isForbidden());

        mvc.perform(patch("/api/admin/products/" + productId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void orderDetailIsAdminOnlyAndReturnsSafeOrderTimeline() throws Exception {
        String adminToken = login(TestAdminCredentials.email(), TestAdminCredentials.password());
        String customerToken = registerCustomer();
        String email = mapper.readTree(
                mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + customerToken))
                        .andReturn().getResponse().getContentAsString()).path("email").asText();
        AppUser customer = users.findByEmailIgnoreCase(email).orElseThrow();

        CustomerOrder order = new CustomerOrder();
        order.setUser(customer);
        order.setRequestId("admin-detail-" + UUID.randomUUID());
        order.setStatus("PENDING_PAYMENT");
        order.setSubtotal(new BigDecimal("100.00"));
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setShipping(new BigDecimal("49.00"));
        order.setTax(new BigDecimal("26.82"));
        order.setTotal(new BigDecimal("175.82"));
        order.setShippingAddress("Test-only address");
        CustomerOrder saved = orders.saveAndFlush(order);

        mvc.perform(get("/api/admin/orders/" + saved.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.timeline").isArray())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        mvc.perform(get("/api/admin/orders/" + saved.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    private String registerCustomer() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String body = mapper.createObjectNode()
                .put("name", "Management test customer")
                .put("email", "management-" + suffix + "@shopit.test")
                .put("password", "Customer-" + suffix + "-Strong!")
                .toString();
        MvcResult result = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private String login(String email, String password) throws Exception {
        String body = mapper.createObjectNode()
                .put("email", email)
                .put("password", password)
                .toString();
        MvcResult result = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }
}
