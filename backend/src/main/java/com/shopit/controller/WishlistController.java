package com.shopit.controller;
import com.shopit.dto.ApiDtos.WishlistLine;
import com.shopit.service.WishlistService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/wishlist")
public class WishlistController{
 private final WishlistService wishlist;public WishlistController(WishlistService w){wishlist=w;}
 @GetMapping public java.util.List<WishlistLine> list(Authentication a){return wishlist.list(a.getName());}
 @PostMapping("/items/{productId}") public java.util.List<WishlistLine> add(Authentication a,@PathVariable Long productId){return wishlist.add(a.getName(),productId);}
 @DeleteMapping("/items/{id}") public java.util.List<WishlistLine> remove(Authentication a,@PathVariable Long id){return wishlist.remove(a.getName(),id);}
}