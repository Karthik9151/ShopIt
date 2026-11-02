package com.shopit.controller;

import com.shopit.dto.ApiDtos.*;
import com.shopit.entity.AppUser;
import com.shopit.entity.Product;
import com.shopit.repository.*;
import com.shopit.service.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final ProductService products;
    private final CategoryService categories;
    private final OrderService orders;
    private final UserRepository users;
    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;
    private final PaymentRepository payments;

    public AdminController(ProductService products, CategoryService categories, OrderService orders,
        UserRepository users, ProductRepository productRepo, OrderRepository orderRepo, PaymentRepository payments) {
        this.products = products;
        this.categories = categories;
        this.orders = orders;
        this.users = users;
        this.productRepo = productRepo;
        this.orderRepo = orderRepo;
        this.payments = payments;
    }

    @GetMapping("/dashboard")
    public DashboardView dashboard() {
        BigDecimal verifiedSales = payments.sumVerifiedSales();
        BigDecimal simulatedValue = payments.sumSimulatedPayments();
        List<Product> inventory = productRepo.findByActiveTrue();
        BigDecimal inventoryCost = inventory.stream()
            .map(p -> p.getCostPrice().multiply(BigDecimal.valueOf(p.getStock())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal potentialRetail = inventory.stream()
            .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getStock())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal estimatedMargin = potentialRetail.subtract(inventoryCost);
        return new DashboardView(users.countByRole("CUSTOMER"), productRepo.countByActiveTrue(),
            orderRepo.count(), payments.countByStatus("SUCCESS"), verifiedSales, simulatedValue,
            orderRepo.countByStatus("PENDING_PAYMENT"), productRepo.countByStockLessThanAndActiveTrue(5),
            inventoryCost, potentialRetail, estimatedMargin);
    }

    @GetMapping("/products")
    public List<AdminProductView> allProducts() { return products.adminList(); }

    @PostMapping("/products")
    public ResponseEntity<AdminProductView> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(products.createAdmin(request));
    }

    @PutMapping("/products/{id}")
    public AdminProductView updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return products.updateAdmin(id, request);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        products.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryView> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categories.create(request));
    }
    @PutMapping("/categories/{id}")
    public CategoryView updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return categories.update(id, request);
    }
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categories.deactivate(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/orders")
    public List<OrderView> allOrders() { return orders.all(); }
    @PutMapping("/orders/{id}/status")
    public OrderView status(@PathVariable Long id, @Valid @RequestBody StatusUpdate request) {
        return orders.updateStatus(id, request.status().toUpperCase(java.util.Locale.ROOT));
    }
    @GetMapping("/customers")
    public Page<CustomerView> customers(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("Invalid pagination");
        return users.findByRole("CUSTOMER", PageRequest.of(page, size, Sort.by("createdAt").descending()))
            .map(this::customerView);
    }
    @PutMapping("/customers/{id}/status")
    public CustomerView updateCustomerStatus(@PathVariable Long id, @Valid @RequestBody CustomerStatusRequest request) {
        AppUser customer = users.findById(id).filter(user -> "CUSTOMER".equals(user.getRole()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        customer.setEnabled(request.enabled());
        return customerView(users.save(customer));
    }
    private CustomerView customerView(AppUser user) {
        return new CustomerView(user.getId(), user.getName(), user.getEmail(), user.getPhone(),
            user.isEnabled(), user.getCreatedAt());
    }
}
