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
    Page<CustomerOrder> findAllByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<CustomerOrder> findAllByOrderByCreatedAtDesc();

    @Query("select o.status, count(o.id) from CustomerOrder o group by o.status order by o.status")
    List<Object[]> countOrdersGroupedByStatus();

    @Query("""
        select oi.productName, coalesce(sum(oi.quantity), 0), coalesce(sum(oi.unitPrice * oi.quantity), 0)
        from OrderItem oi
        where exists (
            select p.id from Payment p
            where p.order = oi.order and p.status = 'SUCCESS'
        )
        group by oi.productName
        order by sum(oi.quantity) desc, oi.productName asc
        """)
    List<Object[]> findTopProductsBySuccessfulOrders(Pageable pageable);

    @Query("""
        select o.id, o.user.name, o.user.email, o.status, o.total, o.createdAt
        from CustomerOrder o
        order by o.createdAt desc
        """)
    List<Object[]> findRecentOrdersForAdmin(Pageable pageable);
    @Query("""
        select o from CustomerOrder o
        where (:status is null or o.status = :status)
          and (:orderId is null or o.id = :orderId)
          and (:customerEmail is null
               or lower(o.user.email) like lower(concat('%', :customerEmail, '%')))
          and (:createdFrom is null or o.createdAt >= :createdFrom)
          and (:createdBefore is null or o.createdAt < :createdBefore)
        """)
    Page<CustomerOrder> searchForAdmin(
            @Param("status") String status,
            @Param("orderId") Long orderId,
            @Param("customerEmail") String customerEmail,
            @Param("createdFrom") Instant createdFrom,
            @Param("createdBefore") Instant createdBefore,
            Pageable pageable);

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
