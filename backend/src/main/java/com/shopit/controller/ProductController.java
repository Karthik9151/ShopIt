package com.shopit.controller;
import com.shopit.dto.ApiDtos.ProductView;
import com.shopit.service.ProductService;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;
import java.util.Set;
@RestController @RequestMapping("/api/products")
public class ProductController{
 private final ProductService service;public ProductController(ProductService s){service=s;}
 @GetMapping public Page<ProductView> list(@RequestParam(required=false)String q,@RequestParam(required=false)Long categoryId,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="12")int size,@RequestParam(defaultValue="createdAt")String sort,@RequestParam(defaultValue="desc")String direction){
  if(page<0||size<1||size>48)throw new IllegalArgumentException("Invalid pagination parameters");
  String key=Set.of("createdAt","name","price").contains(sort)?sort:"createdAt";
  Sort s="asc".equalsIgnoreCase(direction)?Sort.by(key).ascending():Sort.by(key).descending();
  return service.list(q,categoryId,PageRequest.of(page,size,s));
 }
 @GetMapping("/{id}") public ProductView get(@PathVariable Long id){return service.get(id);}
}