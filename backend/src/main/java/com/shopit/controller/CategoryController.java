package com.shopit.controller;
import com.shopit.dto.ApiDtos.CategoryView;
import com.shopit.service.CategoryService;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/categories")
public class CategoryController{
 private final CategoryService service;public CategoryController(CategoryService s){service=s;}
 @GetMapping public java.util.List<CategoryView> list(){return service.list();}
}