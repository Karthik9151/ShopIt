package com.shopit.repository;
import com.shopit.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface ProductRepository extends JpaRepository<Product,Long>{
 Page<Product> findByActiveTrue(Pageable pageable);
 Page<Product> findByActiveTrueAndNameContainingIgnoreCase(String name,Pageable pageable);
 Page<Product> findByActiveTrueAndCategory_Id(Long categoryId,Pageable pageable);
 Page<Product> findByActiveTrueAndCategory_IdAndNameContainingIgnoreCase(Long categoryId,String name,Pageable pageable);
 long countByActiveTrue();
 long countByStockLessThanAndActiveTrue(int stock);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select p from Product p where p.id=:id")
 Optional<Product> findByIdForUpdate(@Param("id")Long id);
}