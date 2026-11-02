package com.shopit.service;
import com.shopit.dto.ApiDtos.*;
import com.shopit.entity.*;
import com.shopit.repository.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository products;
    private final CategoryRepository categories;

    public ProductService(ProductRepository products, CategoryRepository categories) {
        this.products = products;
        this.categories = categories;
    }

    public Page<ProductView> list(String query, Long categoryId, BigDecimal minPrice,
                                  BigDecimal maxPrice, Pageable pageable) {
        if (minPrice != null && minPrice.signum() < 0 || maxPrice != null && maxPrice.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price filters cannot be negative");
        }
        if (minPrice != null && maxPrice != null && maxPrice.compareTo(minPrice) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum price must be at least the minimum price");
        }
        String term = query == null || query.isBlank() ? null : query.trim();
        return products.search(term, categoryId, minPrice, maxPrice, pageable).map(this::view);
    }

    public ProductView get(Long id) {
        Product product = products.findById(id).filter(Product::isActive)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        return view(product);
    }

    public Page<AdminProductView> adminList(String query, Boolean active, Pageable pageable) {
        String term = query == null || query.isBlank() ? null : query.trim();
        return products.searchAdmin(term, active, pageable).map(this::adminView);
    }

    @Transactional
    public AdminProductView createAdmin(ProductRequest request) {
        return adminView(products.save(apply(new Product(), request)));
    }

    @Transactional
    public AdminProductView updateAdmin(Long id, ProductRequest request) {
        Product product = products.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return adminView(products.save(apply(product, request)));
    }

    @Transactional
    public ProductView create(ProductRequest request) {
        return view(products.save(apply(new Product(), request)));
    }

    @Transactional
    public ProductView update(Long id, ProductRequest request) {
        Product product = products.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return view(products.save(apply(product, request)));
    }

    @Transactional
    public void deactivate(Long id) {
        Product product = products.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        product.setActive(false);
    }

    private Product apply(Product product, ProductRequest request) {
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setPrice(request.price());
        if (request.costPrice() != null) product.setCostPrice(request.costPrice());
        product.setStock(request.stock());
        product.setImageUrl(request.imageUrl());
        product.setCategory(request.categoryId() == null ? null : categories.findById(request.categoryId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category")));
        if (request.active() != null) product.setActive(request.active());
        return product;
    }

    public AdminProductView adminView(Product product) {
        Category category = product.getCategory();
        return new AdminProductView(product.getId(), product.getName(), product.getDescription(), product.getPrice(),
            product.getCostPrice(), product.getStock(), product.getImageUrl(), category == null ? null : category.getId(),
            category == null ? null : category.getName(), product.isActive(), product.getCreatedAt());
    }

    public ProductView view(Product product) {
        Category category = product.getCategory();
        int stock = product.getStock();
        return new ProductView(product.getId(), product.getName(), product.getDescription(), product.getPrice(),
            stock > 0, stock > 0 && stock <= 5, product.getImageUrl(),
            category == null ? null : category.getId(),
            category == null ? null : category.getName(), product.isActive(), product.getCreatedAt());
    }
}
