package com.shopit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class AdminDashboardAndCustomerHistoryIntegrationTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    UserRepository users;

    @Autowired
    OrderRepository orders;

    @Test
    void dashboardReturnsGroupedCollectionsAndRemainsAdminOnly() throws Exception {
        String adminToken = login(TestAdminCredentials.email(), TestAdminCredentials.password());

        mvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salesByDay").isArray())
                .andExpect(jsonPath("$.statusBreakdown").isArray())
                .andExpect(jsonPath("$.topProducts").isArray())
                .andExpect(jsonPath("$.lowStockItems").isArray())
                .andExpect(jsonPath("$.recentOrders").isArray())
                .andExpect(jsonPath("$.inventoryCostBudget").exists());

        String customerEmail = "dashboard-customer-" + UUID.randomUUID() + "@shopit.test";
        String customerToken = registerCustomer(customerEmail);

        mvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void customerOrderHistoryIsPagedAndDoesNotExposeAccountSecrets() throws Exception {
        String adminToken = login(TestAdminCredentials.email(), TestAdminCredentials.password());
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String email = "history-customer-" + suffix + "@shopit.test";
        String customerToken = registerCustomer(email);
        AppUser customer = users.findByEmailIgnoreCase(email).orElseThrow();

        CustomerOrder order = new CustomerOrder();
        order.setUser(customer);
        order.setRequestId("history-" + suffix);
        order.setStatus("PENDING_PAYMENT");
        order.setSubtotal(new BigDecimal("250.00"));
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setShipping(new BigDecimal("49.00"));
        order.setTax(new BigDecimal("53.82"));
        order.setTotal(new BigDecimal("352.82"));
        order.setShippingAddress("Test address, not a real delivery location");
        CustomerOrder saved = orders.saveAndFlush(order);

        mvc.perform(get("/api/admin/customers/" + customer.getId() + "/orders")
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(saved.getId()))
                .andExpect(jsonPath("$.content[0].status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.content[0].timeline").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].passwordHash").doesNotExist());

        mvc.perform(get("/api/admin/customers/" + customer.getId() + "/orders")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/admin/customers/" + customer.getId() + "/orders")
                        .param("size", "101")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    private String registerCustomer(String email) throws Exception {
        String password = "Customer-" + UUID.randomUUID() + "-Strong!";
        String body = mapper.createObjectNode()
                .put("name", "Dashboard test customer")
                .put("email", email)
                .put("password", password)
                .toString();
        MvcResult result = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode response = mapper.readTree(result.getResponse().getContentAsString());
        return response.get("token").asText();
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
        return mapper.readTree(result.getResponse().getContentAsString())
                .get("token")
                .asText();
    }
}
