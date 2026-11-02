package com.shopit.repository;
import com.shopit.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("""
        select p from Product p
        where p.active = true
          and (:queryText is null or lower(p.name) like lower(concat('%', :queryText, '%')))
          and (:categoryId is null or p.category.id = :categoryId)
          and (:minPrice is null or p.price >= :minPrice)
          and (:maxPrice is null or p.price <= :maxPrice)
        """)
    Page<Product> search(@Param("queryText") String queryText,
                         @Param("categoryId") Long categoryId,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice,
                         Pageable pageable);

    long countByActiveTrue();
    long countByStockLessThanAndActiveTrue(int stock);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
