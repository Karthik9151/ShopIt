package com.shopit.repository;

import com.shopit.entity.AppUser;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    long countByRole(String role);

    Page<AppUser> findByRole(String role, Pageable pageable);

    @Query("""
        select customer from AppUser customer
        where customer.role = 'CUSTOMER'
          and (
              :searchText is null
              or lower(customer.name) like lower(concat('%', :searchText, '%'))
              or lower(customer.email) like lower(concat('%', :searchText, '%'))
              or lower(coalesce(customer.phone, '')) like lower(concat('%', :searchText, '%'))
          )
        """)
    Page<AppUser> searchCustomers(@Param("searchText") String searchText, Pageable pageable);
}
