package com.shopit.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
public final class ApiDtos {
 private ApiDtos(){}
 public record RegisterRequest(@NotBlank @Size(max=120)String name,@NotBlank @Email @Size(max=190)String email,@NotBlank @Size(min=10,max=72)String password,@Size(max=30)String phone){}
 public record LoginRequest(@NotBlank @Email String email,@NotBlank String password){}
 public record UserView(Long id,String name,String email,String role){}
 public record AuthResponse(String token,UserView user,long expiresInMs){}
 public record CategoryView(Long id,String name,String slug,String description){}
 public record CategoryRequest(@NotBlank @Size(max=100)String name,@Size(max=500)String description){}
 public record ProductRequest(@NotBlank @Size(max=180)String name,@Size(max=10000)String description,@NotNull @DecimalMin("0.00") @Digits(integer=10,fraction=2)BigDecimal price,@Min(0) @Max(1000000)int stock,@Size(max=1000)String imageUrl,Long categoryId,Boolean active){}
 public record ProductView(Long id,String name,String description,BigDecimal price,int stock,String imageUrl,Long categoryId,String categoryName,boolean active,Instant createdAt){}
 public record CartAddRequest(@NotNull Long productId,@Min(1) @Max(100)int quantity){}
 public record CartUpdateRequest(@Min(1) @Max(100)int quantity){}
 public record CartLine(Long id,Long productId,String name,BigDecimal unitPrice,int quantity,BigDecimal lineTotal,String imageUrl,int availableStock){}
 public record CartView(List<CartLine> items,BigDecimal subtotal,int itemCount){}
 public record WishlistLine(Long id,Long productId,String name,BigDecimal price,String imageUrl){}
 public record CheckoutRequest(@NotBlank @Size(max=1000)String shippingAddress,@NotBlank @Size(max=80)String requestId){}
 public record OrderLine(Long productId,String productName,BigDecimal unitPrice,int quantity,BigDecimal lineTotal){}
 public record OrderView(Long id,String status,BigDecimal subtotal,BigDecimal shipping,BigDecimal tax,BigDecimal total,String shippingAddress,Instant createdAt,List<OrderLine> items){}
 public record PaymentView(Long id,Long orderId,String provider,String status,String reference,BigDecimal amount,Instant paidAt){}
 public record StatusUpdate(@NotBlank String status){}
 public record DashboardView(long customers,long products,long orders,long paidPayments,BigDecimal verifiedSales,long pendingOrders,long lowStockProducts){}
}