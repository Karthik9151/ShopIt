package com.shopit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopit.entity.Category;
import com.shopit.entity.Product;
import com.shopit.repository.CategoryRepository;
import com.shopit.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("ci")
class InactiveCategoryVisibilityIntegrationTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    CategoryRepository categories;

    @Autowired
    ProductRepository products;

    @Test
    void publicCatalogueAndProductDetailHideProductsInInactiveCategories() throws Exception {
        String unique = UUID.randomUUID().toString().replace("-", "");

        Category inactiveCategory = category("Inactive " + unique, "inactive-" + unique, false);
        Category activeCategory = category("Active " + unique, "active-" + unique, true);

        Product hiddenProduct = product("Hidden " + unique, inactiveCategory);
        Product visibleProduct = product("Visible " + unique, activeCategory);
        Product uncategorizedProduct = product("Uncategorized " + unique, null);

        MvcResult catalogue = mvc.perform(get("/api/products")
                        .param("q", unique)
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode page = mapper.readTree(catalogue.getResponse().getContentAsString());
        JsonNode content = page.get("content");

        assertThat(containsProduct(content, hiddenProduct.getId())).isFalse();
        assertThat(containsProduct(content, visibleProduct.getId())).isTrue();
        assertThat(containsProduct(content, uncategorizedProduct.getId())).isTrue();

        mvc.perform(get("/api/products/" + hiddenProduct.getId()))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/products/" + visibleProduct.getId()))
                .andExpect(status().isOk());
        mvc.perform(get("/api/products/" + uncategorizedProduct.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void publicProductSearchRejectsOversizedQuery() throws Exception {
        mvc.perform(get("/api/products").param("q", "x".repeat(121)))
                .andExpect(status().isBadRequest());
    }

    private Category category(String name, String slug, boolean active) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        category.setDescription("Visibility regression test category");
        category.setActive(active);
        return categories.saveAndFlush(category);
    }

    private Product product(String name, Category category) {
        Product product = new Product();
        product.setName(name);
        product.setDescription("Visibility regression test product");
        product.setPrice(new BigDecimal("12.34"));
        product.setCostPrice(BigDecimal.ZERO);
        product.setStock(8);
        product.setActive(true);
        product.setCategory(category);
        return products.saveAndFlush(product);
    }

    private boolean containsProduct(JsonNode products, long productId) {
        for (JsonNode product : products) {
            if (product.get("id").asLong() == productId) {
                return true;
            }
        }
        return false;
    }
}
