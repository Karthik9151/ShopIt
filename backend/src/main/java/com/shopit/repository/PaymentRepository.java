package com.shopit.repository;
import com.shopit.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder_Id(Long orderId);
    Optional<Payment> findByOrder_IdAndStatus(Long orderId, String status);
    Optional<Payment> findByIdAndOrder_User_Id(Long id, Long userId);
    long countByStatus(String status);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.status = 'SUCCESS' and p.provider <> 'SIMULATED'")
    BigDecimal sumVerifiedSales();

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.status = 'SUCCESS' and p.provider = 'SIMULATED'")
    BigDecimal sumSimulatedPayments();

    @Query("""
        select function('date', p.paidAt), coalesce(sum(p.amount), 0)
        from Payment p
        where p.status = 'SUCCESS'
          and p.provider <> 'SIMULATED'
          and p.paidAt is not null
          and p.paidAt >= :since
        group by function('date', p.paidAt)
        order by function('date', p.paidAt)
        """)
    List<Object[]> sumVerifiedSalesByDaySince(@org.springframework.data.repository.query.Param("since") Instant since);
}
