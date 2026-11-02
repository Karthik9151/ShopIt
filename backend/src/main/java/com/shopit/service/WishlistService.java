package com.shopit.service;
import com.shopit.dto.ApiDtos.WishlistLine;
import com.shopit.entity.*;
import com.shopit.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
@Service @Transactional
public class WishlistService{
 private final WishlistItemRepository items;private final UserRepository users;private final ProductRepository products;
 public WishlistService(WishlistItemRepository i,UserRepository u,ProductRepository p){items=i;users=u;products=p;}
 public java.util.List<WishlistLine> list(String email){AppUser u=user(email);return items.findByUser_Id(u.getId()).stream().map(i->{
  Product p=i.getProduct();
  boolean available=p.isActive()&&p.getStock()>0;
  String reason=p.isActive()?(available?null:"Currently out of stock"):"This product is no longer available";
  return new WishlistLine(i.getId(),p.getId(),p.getName(),p.getPrice(),p.getImageUrl(),available,reason);
 }).toList();}
 public java.util.List<WishlistLine> add(String email,Long productId){AppUser u=user(email);Product p=products.findById(productId).filter(Product::isActive).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found"));if(items.findByUser_IdAndProduct_Id(u.getId(),productId).isEmpty()){WishlistItem i=new WishlistItem();i.setUser(u);i.setProduct(p);items.save(i);}return list(email);}
 public java.util.List<WishlistLine> remove(String email,Long id){AppUser u=user(email);WishlistItem i=items.findByIdAndUser_Id(id,u.getId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Wishlist item not found"));items.delete(i);return list(email);}
 private AppUser user(String email){return users.findByEmailIgnoreCase(email).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED));}
}