package com.shopit.controller;
import com.shopit.dto.ApiDtos.*;
import com.shopit.service.CartService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/cart")
public class CartController{
 private final CartService cart;public CartController(CartService c){cart=c;}
 @GetMapping public CartView view(Authentication a){return cart.view(a.getName());}
 @PostMapping("/items") public CartView add(Authentication a,@Valid @RequestBody CartAddRequest r){return cart.add(a.getName(),r);}
 @PutMapping("/items/{id}") public CartView update(Authentication a,@PathVariable Long id,@Valid @RequestBody CartUpdateRequest r){return cart.update(a.getName(),id,r);}
 @DeleteMapping("/items/{id}") public CartView remove(Authentication a,@PathVariable Long id){return cart.remove(a.getName(),id);}
 @DeleteMapping public CartView clear(Authentication a){return cart.clear(a.getName());}
}