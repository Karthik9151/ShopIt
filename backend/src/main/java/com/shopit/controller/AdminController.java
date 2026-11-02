package com.shopit.controller;

import com.shopit.dto.ApiDtos.*;
import com.shopit.entity.AppUser;
import com.shopit.entity.Product;
import com.shopit.repository.*;
import com.shopit.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.Instant;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Validated
public class AdminController {
    private final ProductService products;
    private final CategoryService categories;
    private final OrderService orders;
    private final UserRepository users;
    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;
    private final PaymentRepository payments;
    private final AdminCsvExportService csvExports;

    public AdminController(ProductService products, CategoryService categories, OrderService orders,
        UserRepository users, ProductRepository productRepo, OrderRepository orderRepo,
        PaymentRepository payments, AdminCsvExportService csvExports) {
        this.products = products;
        this.categories = categories;
        this.orders = orders;
        this.users = users;
        this.productRepo = productRepo;
        this.orderRepo = orderRepo;
        this.payments = payments;
        this.csvExports = csvExports;
    }

    @GetMapping("/dashboard")
    public DashboardView dashboard() {
        BigDecimal verifiedSales = payments.sumVerifiedSales();
        BigDecimal simulatedValue = payments.sumSimulatedPayments();
        // Include inactive SKUs too: unsold stock still ties up the owner's capital.
        List<Product> inventory = productRepo.findAll();
        BigDecimal inventoryCost = inventory.stream()
            .map(p -> p.getCostPrice().multiply(BigDecimal.valueOf(p.getStock())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal potentialRetail = inventory.stream()
            .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getStock())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal estimatedMargin = potentialRetail.subtract(inventoryCost);
        List<SalesByDayView> salesByDay = payments.sumVerifiedSalesByDaySince(
                Instant.now().minus(java.time.Duration.ofDays(29)))
            .stream()
            .map(row -> new SalesByDayView(row[0].toString(), (BigDecimal) row[1]))
            .toList();
        List<OrderStatusCountView> statusBreakdown = orderRepo.countOrdersGroupedByStatus()
            .stream()
            .map(row -> new OrderStatusCountView((String) row[0], ((Number) row[1]).longValue()))
            .toList();
        List<TopProductView> topProducts = orderRepo.findTopProductsBySuccessfulOrders(PageRequest.of(0, 5))
            .stream()
            .map(row -> new TopProductView(
                (String) row[0],
                ((Number) row[1]).longValue(),
                (BigDecimal) row[2]))
            .toList();
        List<LowStockProductView> lowStockItems = productRepo.findLowStockForAdmin(5, PageRequest.of(0, 6))
            .stream()
            .map(row -> new LowStockProductView(
                ((Number) row[0]).longValue(),
                (String) row[1],
                ((Number) row[2]).intValue()))
            .toList();
        List<RecentOrderView> recentOrders = orderRepo.findRecentOrdersForAdmin(PageRequest.of(0, 6))
            .stream()
            .map(row -> new RecentOrderView(
                ((Number) row[0]).longValue(),
                (String) row[1],
                (String) row[2],
                (String) row[3],
                (BigDecimal) row[4],
                (Instant) row[5]))
            .toList();
        return new DashboardView(users.countByRole("CUSTOMER"), productRepo.countByActiveTrue(),
            orderRepo.count(), payments.countByStatus("SUCCESS"), verifiedSales, simulatedValue,
            orderRepo.countByStatus("PENDING_PAYMENT"), productRepo.countByStockLessThanAndActiveTrue(5),
            inventoryCost, potentialRetail, estimatedMargin,
            salesByDay, statusBreakdown, topProducts, lowStockItems, recentOrders);
    }

    @GetMapping("/products")
    public Page<AdminProductView> allProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean active) {
        Pageable pageable = adminPage(page, size);
        return products.adminList(q, active, pageable);
    }

    @PostMapping("/products")
    public ResponseEntity<AdminProductView> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(products.createAdmin(request));
    }

    @PutMapping("/products/{id}")
    public AdminProductView updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return products.updateAdmin(id, request);
    }

    @PatchMapping("/products/{id}/stock")
    public AdminProductView updateProductStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {
        return products.updateStock(id, request.stock());
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        products.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories")
    public List<AdminCategoryView> allCategories() {
        return categories.adminList();
    }

    @PatchMapping("/categories/{id}/status")
    public AdminCategoryView updateCategoryStatus(
            @PathVariable Long id,
            @Valid @RequestBody CategoryStatusRequest request) {
        return categories.setActive(id, request.active());
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
    public Page<OrderView> allOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) @Size(max = 190) String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        Pageable pageable = adminPage(page, size);
        return orders.adminList(status, orderId, email, from, to, pageable);
    }
    @GetMapping(value = "/orders/export.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) @Size(max = 190) String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        String csv = csvExports.ordersCsv(status, orderId, email, from, to);
        return csvResponse("shopit-orders.csv", csv);
    }

    @PutMapping("/orders/{id}/status")
    public OrderView status(@PathVariable Long id, @Valid @RequestBody StatusUpdate request) {
        return orders.updateStatus(id, request.status().toUpperCase(java.util.Locale.ROOT));
    }
    @GetMapping("/customers")
    public Page<CustomerView> customers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @Size(max = 120) String q) {
        Pageable pageable = adminPage(page, size);
        String searchText = q == null || q.isBlank() ? null : q.trim();
        return users.searchCustomers(searchText, pageable).map(this::customerView);
    }

    @GetMapping("/customers/{id}")
    public CustomerView customerDetail(@PathVariable Long id) {
        AppUser customer = users.findById(id)
                .filter(user -> "CUSTOMER".equals(user.getRole()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        return customerView(customer);
    }
    @GetMapping("/customers/{id}/orders")
    public Page<OrderView> customerOrders(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        users.findById(id)
            .filter(user -> "CUSTOMER".equals(user.getRole()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        return orders.adminOrdersForCustomer(id, adminPage(page, size));
    }

    @PutMapping("/customers/{id}/status")
    public CustomerView updateCustomerStatus(@PathVariable Long id, @Valid @RequestBody CustomerStatusRequest request) {
        AppUser customer = users.findById(id).filter(user -> "CUSTOMER".equals(user.getRole()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        customer.setEnabled(request.enabled());
        return customerView(users.save(customer));
    }
    @GetMapping(value = "/inventory/export.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportInventory(
            @RequestParam(required = false) Integer threshold,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        String csv = csvExports.inventoryCsv(threshold, sort, direction);
        return csvResponse("shopit-inventory.csv", csv);
    }

    private ResponseEntity<byte[]> csvResponse(String filename, String csv) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(csv.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private Pageable adminPage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid pagination");
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private CustomerView customerView(AppUser user) {
        return new CustomerView(user.getId(), user.getName(), user.getEmail(), user.getPhone(),
            user.isEnabled(), user.getCreatedAt());
    }
}
