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
        select user from AppUser user
        where user.role = 'CUSTOMER'
          and (
              :searchText is null
              or lower(user.name) like lower(concat('%', :searchText, '%'))
              or lower(user.email) like lower(concat('%', :searchText, '%'))
              or lower(coalesce(user.phone, '')) like lower(concat('%', :searchText, '%'))
          )
        """)
    Page<AppUser> searchCustomers(@Param("searchText") String searchText, Pageable pageable);
}
