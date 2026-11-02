package com.shopit.repository;
import com.shopit.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface PaymentRepository extends JpaRepository<Payment,Long>{
 Optional<Payment> findByOrder_Id(Long orderId);
 Optional<Payment> findByIdAndOrder_User_Id(Long id,Long userId);
 long countByStatus(String status);
}