package com.shopit.controller;
import com.shopit.dto.ApiDtos.*;
import com.shopit.repository.*;
import com.shopit.service.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

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
        return new DashboardView(users.countByRole("CUSTOMER"), productRepo.countByActiveTrue(),
            orderRepo.count(), payments.countByStatus("SUCCESS"), verifiedSales, simulatedValue,
            orderRepo.countByStatus("PENDING_PAYMENT"), productRepo.countByStockLessThanAndActiveTrue(5));
    }

    @PostMapping("/products")
    public ResponseEntity<ProductView> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(products.create(request));
    }
    @PutMapping("/products/{id}")
    public ProductView updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return products.update(id, request);
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
    public java.util.List<OrderView> allOrders() { return orders.all(); }
    @PutMapping("/orders/{id}/status")
    public OrderView status(@PathVariable Long id, @Valid @RequestBody StatusUpdate request) {
        return orders.updateStatus(id, request.status().toUpperCase(java.util.Locale.ROOT));
    }
    @GetMapping("/customers")
    public Page<UserView> customers(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("Invalid pagination");
        return users.findByRole("CUSTOMER", PageRequest.of(page, size, Sort.by("createdAt").descending()))
            .map(user -> new UserView(user.getId(), user.getName(), user.getEmail(), user.getRole()));
    }
}
