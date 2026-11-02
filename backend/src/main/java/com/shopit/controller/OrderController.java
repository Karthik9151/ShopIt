package com.shopit.controller;
import com.shopit.dto.ApiDtos.*;
import com.shopit.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/orders")
public class OrderController{
 private final OrderService orders;public OrderController(OrderService o){orders=o;}
 @PostMapping public ResponseEntity<OrderView> checkout(Authentication a,@Valid @RequestBody CheckoutRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(orders.checkout(a.getName(),r));}
 @GetMapping("/my") public java.util.List<OrderView> mine(Authentication a){return orders.mine(a.getName());}
 @GetMapping("/{id}") public OrderView get(Authentication a,@PathVariable Long id){return orders.getMine(a.getName(),id);}
 @PostMapping("/{id}/cancel") public OrderView cancel(Authentication a,@PathVariable Long id){return orders.cancel(a.getName(),id);}
}