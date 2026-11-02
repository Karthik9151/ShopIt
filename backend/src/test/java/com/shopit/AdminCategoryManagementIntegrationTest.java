package com.shopit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class AdminCategoryManagementIntegrationTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @Test
    void adminCanViewCategoryCountsDeactivateAndReactivateCategories() throws Exception {
        String adminToken = login(TestAdminCredentials.email(), TestAdminCredentials.password());
        String suffix = UUID.randomUUID().toString().replace("-", "");

        String categoryRequest = mapper.createObjectNode()
                .put("name", "Managed category " + suffix)
                .put("description", "Admin category management integration test")
                .toString();
        MvcResult created = mvc.perform(post("/api/admin/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryRequest))
                .andExpect(status().isCreated())
                .andReturn();
        long categoryId = mapper.readTree(created.getResponse().getContentAsString())
                .get("id")
                .asLong();

        String productRequest = mapper.createObjectNode()
                .put("name", "Category count product " + suffix)
                .put("description", "Category count integration test")
                .put("price", 50.00)
                .put("stock", 4)
                .put("categoryId", categoryId)
                .put("active", true)
                .toString();
        mvc.perform(post("/api/admin/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequest))
                .andExpect(status().isCreated());

        MvcResult list = mvc.perform(get("/api/admin/categories")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode category = findCategory(mapper.readTree(list.getResponse().getContentAsString()), categoryId);
        assertThat(category.get("active").asBoolean()).isTrue();
        assertThat(category.get("productCount").asLong()).isEqualTo(1);

        mvc.perform(delete("/api/admin/categories/" + categoryId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + categoryId + ")]").doesNotExist());
        mvc.perform(get("/api/products")
                        .param("q", suffix))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));

        mvc.perform(patch("/api/admin/categories/" + categoryId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.productCount").value(1));

        mvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + categoryId + ")]").exists());
        mvc.perform(get("/api/products").param("q", suffix))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    private JsonNode findCategory(JsonNode categories, long id) {
        for (JsonNode category : categories) {
            if (category.get("id").asLong() == id) {
                return category;
            }
        }
        throw new AssertionError("Admin category was not found");
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
