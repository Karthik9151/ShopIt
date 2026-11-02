package com.shopit.service;
import com.shopit.dto.ApiDtos.*;
import com.shopit.entity.*;
import com.shopit.repository.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
@Service @Transactional(readOnly=true)
public class ProductService{
 private final ProductRepository products;private final CategoryRepository categories;
 public ProductService(ProductRepository p,CategoryRepository c){products=p;categories=c;}
 public Page<ProductView> list(String q,Long category,Pageable pageable){
  String term=q==null?"":q.trim();Page<Product> p;
  if(category!=null&&!term.isBlank())p=products.findByActiveTrueAndCategory_IdAndNameContainingIgnoreCase(category,term,pageable);
  else if(category!=null)p=products.findByActiveTrueAndCategory_Id(category,pageable);
  else if(!term.isBlank())p=products.findByActiveTrueAndNameContainingIgnoreCase(term,pageable);
  else p=products.findByActiveTrue(pageable);
  return p.map(this::view);
 }
 public ProductView get(Long id){return view(products.findById(id).filter(Product::isActive).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found")));}
 @Transactional public ProductView create(ProductRequest r){return view(products.save(apply(new Product(),r)));}
 @Transactional public ProductView update(Long id,ProductRequest r){Product p=products.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));return view(products.save(apply(p,r)));}
 @Transactional public void deactivate(Long id){Product p=products.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));p.setActive(false);}
 private Product apply(Product p,ProductRequest r){
  p.setName(r.name().trim());p.setDescription(r.description());p.setPrice(r.price());p.setStock(r.stock());p.setImageUrl(r.imageUrl());
  p.setCategory(r.categoryId()==null?null:categories.findById(r.categoryId()).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid category")));
  if(r.active()!=null)p.setActive(r.active());return p;
 }
 public ProductView view(Product p){Category c=p.getCategory();return new ProductView(p.getId(),p.getName(),p.getDescription(),p.getPrice(),p.getStock(),p.getImageUrl(),c==null?null:c.getId(),c==null?null:c.getName(),p.isActive(),p.getCreatedAt());}
}