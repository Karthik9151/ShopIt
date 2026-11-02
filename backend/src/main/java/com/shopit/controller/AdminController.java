package com.shopit.controller;
import com.shopit.dto.ApiDtos.*;
import com.shopit.entity.Payment;
import com.shopit.repository.*;
import com.shopit.service.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
@RestController @RequestMapping("/api/admin")
public class AdminController{
 private final ProductService products;private final CategoryService categories;private final OrderService orders;private final UserRepository users;private final ProductRepository productRepo;private final OrderRepository orderRepo;private final PaymentRepository payments;
 public AdminController(ProductService p,CategoryService c,OrderService o,UserRepository u,ProductRepository pr,OrderRepository or,PaymentRepository pay){products=p;categories=c;orders=o;users=u;productRepo=pr;orderRepo=or;payments=pay;}
 @GetMapping("/dashboard") public DashboardView dashboard(){BigDecimal sales=payments.findAll().stream().filter(p->"SUCCESS".equals(p.getStatus())).map(Payment::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add);return new DashboardView(users.countByRole("CUSTOMER"),productRepo.countByActiveTrue(),orderRepo.count(),payments.countByStatus("SUCCESS"),sales,orderRepo.countByStatus("PENDING_PAYMENT"),productRepo.countByStockLessThanAndActiveTrue(5));}
 @PostMapping("/products") public ResponseEntity<ProductView> createProduct(@Valid @RequestBody ProductRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(products.create(r));}
 @PutMapping("/products/{id}") public ProductView updateProduct(@PathVariable Long id,@Valid @RequestBody ProductRequest r){return products.update(id,r);}
 @DeleteMapping("/products/{id}") public ResponseEntity<Void> deleteProduct(@PathVariable Long id){products.deactivate(id);return ResponseEntity.noContent().build();}
 @PostMapping("/categories") public ResponseEntity<CategoryView> createCategory(@Valid @RequestBody CategoryRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(categories.create(r));}
 @PutMapping("/categories/{id}") public CategoryView updateCategory(@PathVariable Long id,@Valid @RequestBody CategoryRequest r){return categories.update(id,r);}
 @DeleteMapping("/categories/{id}") public ResponseEntity<Void> deleteCategory(@PathVariable Long id){categories.deactivate(id);return ResponseEntity.noContent().build();}
 @GetMapping("/orders") public java.util.List<OrderView> allOrders(){return orders.all();}
 @PutMapping("/orders/{id}/status") public OrderView status(@PathVariable Long id,@Valid @RequestBody StatusUpdate r){return orders.updateStatus(id,r.status().toUpperCase(java.util.Locale.ROOT));}
 @GetMapping("/customers") public Page<UserView> customers(@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size){if(page<0||size<1||size>100)throw new IllegalArgumentException("Invalid pagination");return users.findAll(PageRequest.of(page,size,Sort.by("createdAt").descending())).map(u->new UserView(u.getId(),u.getName(),u.getEmail(),u.getRole()));}
}