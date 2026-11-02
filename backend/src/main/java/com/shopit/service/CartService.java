package com.shopit.service;
import com.shopit.dto.ApiDtos.*;
import com.shopit.entity.*;
import com.shopit.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;
@Service @Transactional
public class CartService{
 private final CartItemRepository cart;
 private final UserRepository users;
 private final ProductRepository products;
 private final PricingService pricing;

 public CartService(CartItemRepository cart,UserRepository users,ProductRepository products,PricingService pricing){
  this.cart=cart;this.users=users;this.products=products;this.pricing=pricing;
 }
 public CartView view(String email){
  AppUser u=user(email);
  List<CartLine> lines=cart.findByUser_Id(u.getId()).stream().map(this::line).toList();
  BigDecimal subtotal=lines.stream().map(CartLine::lineTotal).reduce(BigDecimal.ZERO,BigDecimal::add);
  PricingService.PriceBreakdown totals=pricing.calculate(subtotal,BigDecimal.ZERO);
  return new CartView(lines,subtotal,lines.stream().mapToInt(CartLine::quantity).sum(),
   totals.shipping(),totals.tax(),totals.total(),totals.freeShippingRemaining());
 }
 public CartView add(String email,CartAddRequest r){AppUser u=user(email);Product p=products.findById(r.productId()).filter(Product::isActive).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found"));CartItem item=cart.findByUser_IdAndProduct_Id(u.getId(),p.getId()).orElseGet(()->{CartItem n=new CartItem();n.setUser(u);n.setProduct(p);n.setQuantity(0);return n;});int quantity=item.getQuantity()+r.quantity();if(quantity>p.getStock())throw new ResponseStatusException(HttpStatus.CONFLICT,"Requested quantity exceeds available stock");item.setQuantity(quantity);cart.save(item);return view(email);}
 public CartView update(String email,Long id,CartUpdateRequest r){AppUser u=user(email);CartItem item=cart.findByIdAndUser_Id(id,u.getId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Cart item not found"));if(r.quantity()>item.getProduct().getStock())throw new ResponseStatusException(HttpStatus.CONFLICT,"Requested quantity exceeds available stock");item.setQuantity(r.quantity());return view(email);}
 public CartView remove(String email,Long id){AppUser u=user(email);CartItem item=cart.findByIdAndUser_Id(id,u.getId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Cart item not found"));cart.delete(item);return view(email);}
 public CartView clear(String email){cart.deleteByUser_Id(user(email).getId());return view(email);}
 private CartLine line(CartItem i){Product p=i.getProduct();return new CartLine(i.getId(),p.getId(),p.getName(),p.getPrice(),i.getQuantity(),p.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())),p.getImageUrl(),p.getStock());}
 private AppUser user(String email){return users.findByEmailIgnoreCase(email).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED));}
}