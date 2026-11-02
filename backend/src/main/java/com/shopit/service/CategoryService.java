package com.shopit.service;

import com.shopit.dto.ApiDtos.AdminCategoryView;
import com.shopit.dto.ApiDtos.CategoryRequest;
import com.shopit.dto.ApiDtos.CategoryView;
import com.shopit.entity.Category;
import com.shopit.repository.CategoryRepository;
import com.shopit.repository.ProductRepository;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CategoryService {
    private final CategoryRepository categories;
    private final ProductRepository products;

    public CategoryService(CategoryRepository categories, ProductRepository products) {
        this.categories = categories;
        this.products = products;
    }

    public List<CategoryView> list() {
        return categories.findByActiveTrueOrderByNameAsc().stream().map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public List<AdminCategoryView> adminList() {
        return categories.findAllByOrderByNameAsc().stream()
                .map(this::adminView)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<AdminCategoryView> adminPage(Pageable pageable) {
        return categories.findAllByOrderByNameAsc(pageable).map(this::adminView);
    }

    private AdminCategoryView adminView(Category category) {
        return new AdminCategoryView(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.isActive(),
                products.countByCategory_Id(category.getId()));
    }

    @Transactional
    public CategoryView create(CategoryRequest request) {
        Category category = new Category();
        apply(category, request);
        return view(categories.save(category));
    }

    @Transactional
    public CategoryView update(Long id, CategoryRequest request) {
        Category category = categories.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        apply(category, request);
        return view(categories.save(category));
    }

    @Transactional
    public void deactivate(Long id) {
        Category category = categories.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        category.setActive(false);
    }

    @Transactional
    public AdminCategoryView setActive(Long id, boolean active) {
        Category category = categories.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        category.setActive(active);
        Category saved = categories.save(category);
        return new AdminCategoryView(
                saved.getId(),
                saved.getName(),
                saved.getSlug(),
                saved.getDescription(),
                saved.isActive(),
                products.countByCategory_Id(saved.getId()));
    }

    private void apply(Category category, CategoryRequest request) {
        String normalizedName = request.name().trim();
        category.setName(normalizedName);
        category.setDescription(request.description());
        String slug = Normalizer.normalize(normalizedName.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        category.setSlug(slug);
    }

    private CategoryView view(Category category) {
        return new CategoryView(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription());
    }
}
