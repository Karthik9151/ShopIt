package com.shopit.repository;

import com.shopit.entity.CustomerOrder;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<CustomerOrder,Long> {
    Optional<CustomerOrder> findByUser_IdAndRequestId(Long userId,String requestId);
    Optional<CustomerOrder> findByIdAndUser_Id(Long id,Long userId);
    List<CustomerOrder> findByUser_IdOrderByCreatedAtDesc(Long userId);
    List<CustomerOrder> findAllByOrderByCreatedAtDesc();
    @Query("""
        select o from CustomerOrder o
        where (:status is null or o.status = :status)
        """)
    Page<CustomerOrder> searchForAdmin(@Param("status") String status, Pageable pageable);

    long countByStatus(String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from CustomerOrder o where o.id = :id")
    Optional<CustomerOrder> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from CustomerOrder o where o.id = :id and o.user.id = :userId")
    Optional<CustomerOrder> findByIdAndUser_IdForUpdate(@Param("id") Long id, @Param("userId") Long userId);

    @Query("""
        select o.id from CustomerOrder o
        where o.status = 'PENDING_PAYMENT'
          and o.createdAt <= :cutoff
          and not exists (
              select p.id from Payment p
              where p.order = o and p.status = 'SUCCESS'
          )
        order by o.createdAt asc
        """)
    List<Long> findExpiredUnpaidOrderIds(@Param("cutoff") Instant cutoff);
}
