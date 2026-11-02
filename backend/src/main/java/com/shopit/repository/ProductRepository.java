package com.shopit.repository;

import com.shopit.entity.Product;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
        select p from Product p
        left join p.category publicCategory
        where p.active = true
          and (publicCategory is null or publicCategory.active = true)
          and (:queryText is null or lower(p.name) like lower(concat('%', :queryText, '%')))
          and (:categoryId is null or publicCategory.id = :categoryId)
          and (:minPrice is null or p.price >= :minPrice)
          and (:maxPrice is null or p.price <= :maxPrice)
        """)
    Page<Product> search(
            @Param("queryText") String queryText,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @Query("""
        select p from Product p
        where (:queryText is null or lower(p.name) like lower(concat('%', :queryText, '%')))
          and (:active is null or p.active = :active)
        """)
    Page<Product> searchAdmin(
            @Param("queryText") String queryText,
            @Param("active") Boolean active,
            Pageable pageable);

    long countByActiveTrue();\n\n    long countByCategory_Id(Long categoryId);

    java.util.List<Product> findByActiveTrue();

    long countByStockLessThanAndActiveTrue(int stock);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
