package com.shopit.repository;
import com.shopit.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface WishlistItemRepository extends JpaRepository<WishlistItem,Long>{
 List<WishlistItem> findByUser_Id(Long userId);
 Optional<WishlistItem> findByUser_IdAndProduct_Id(Long userId,Long productId);
 Optional<WishlistItem> findByIdAndUser_Id(Long id,Long userId);
}