package com.shopit.controller;

import com.shopit.dto.ApiDtos.ProductView;
import com.shopit.service.ProductService;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public Page<ProductView> list(
            @RequestParam(required = false) @Size(max = 120) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Invalid pagination parameters");
        }
        String key = Set.of("createdAt", "name", "price").contains(sort) ? sort : "createdAt";
        Sort ordering = "asc".equalsIgnoreCase(direction)
                ? Sort.by(key).ascending()
                : Sort.by(key).descending();
        return service.list(q, categoryId, minPrice, maxPrice, PageRequest.of(page, size, ordering));
    }

    @GetMapping("/{id}")
    public ProductView get(@PathVariable Long id) {
        return service.get(id);
    }
}
