package com.shopit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopit.support.TestAdminCredentials;
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
class AdminStockUpdateIntegrationTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @Test
    void adminCanUpdateStockWithinBoundsButCustomersCannot() throws Exception {
        String adminToken = login(TestAdminCredentials.email(), TestAdminCredentials.password());
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String customerEmail = "stock-" + suffix + "@shopit.test";
        String customerPassword = "Unique-Stock-" + suffix + "!";

        String registration = mapper.createObjectNode()
                .put("name", "Stock Test Customer")
                .put("email", customerEmail)
                .put("password", customerPassword)
                .toString();
        MvcResult registrationResult = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registration))
                .andExpect(status().isCreated())
                .andReturn();
        String customerToken = mapper.readTree(registrationResult.getResponse().getContentAsString())
                .get("token")
                .asText();

        String productRequest = mapper.createObjectNode()
                .put("name", "Stock endpoint " + suffix)
                .put("description", "Admin stock update test")
                .put("price", 99.00)
                .put("stock", 5)
                .put("active", true)
                .toString();
        MvcResult productResult = mvc.perform(post("/api/admin/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequest))
                .andExpect(status().isCreated())
                .andReturn();
        long productId = mapper.readTree(productResult.getResponse().getContentAsString())
                .get("id")
                .asLong();

        mvc.perform(patch("/api/admin/products/" + productId + "/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\": 12}"))
                .andExpect(status().isUnauthorized());

        mvc.perform(patch("/api/admin/products/" + productId + "/stock")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\": 12}"))
                .andExpect(status().isForbidden());

        mvc.perform(patch("/api/admin/products/" + productId + "/stock")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\": 12}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(12));

        mvc.perform(patch("/api/admin/products/" + productId + "/stock")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\": 1000001}"))
                .andExpect(status().isBadRequest());

        mvc.perform(patch("/api/admin/products/" + productId + "/stock")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\": -1}"))
                .andExpect(status().isBadRequest());
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
        JsonNode response = mapper.readTree(result.getResponse().getContentAsString());
        return response.get("token").asText();
    }
}
