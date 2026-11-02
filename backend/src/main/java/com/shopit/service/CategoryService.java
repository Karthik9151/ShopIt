package com.shopit.service;
import com.shopit.dto.ApiDtos.*;
import com.shopit.entity.Category;
import com.shopit.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.text.Normalizer;
import java.util.Locale;
@Service
public class CategoryService{
 private final CategoryRepository repo;public CategoryService(CategoryRepository r){repo=r;}
 public java.util.List<CategoryView> list(){return repo.findByActiveTrueOrderByNameAsc().stream().map(this::view).toList();}
 @Transactional public CategoryView create(CategoryRequest r){Category c=new Category();apply(c,r);return view(repo.save(c));}
 @Transactional public CategoryView update(Long id,CategoryRequest r){Category c=repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));apply(c,r);return view(repo.save(c));}
 @Transactional public void deactivate(Long id){Category c=repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));c.setActive(false);}
 private void apply(Category c,CategoryRequest r){c.setName(r.name().trim());c.setDescription(r.description());String slug=Normalizer.normalize(r.name().trim().toLowerCase(Locale.ROOT),Normalizer.Form.NFD).replaceAll("\\p{M}","").replaceAll("[^a-z0-9]+","-").replaceAll("(^-|-$)","");c.setSlug(slug);}
 private CategoryView view(Category c){return new CategoryView(c.getId(),c.getName(),c.getSlug(),c.getDescription());}
}