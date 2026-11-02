package com.shopit.repository;
import com.shopit.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface OrderRepository extends JpaRepository<CustomerOrder,Long>{
 Optional<CustomerOrder> findByUser_IdAndRequestId(Long userId,String requestId);
 Optional<CustomerOrder> findByIdAndUser_Id(Long id,Long userId);
 List<CustomerOrder> findByUser_IdOrderByCreatedAtDesc(Long userId);
 List<CustomerOrder> findAllByOrderByCreatedAtDesc();
 long countByStatus(String status);
}