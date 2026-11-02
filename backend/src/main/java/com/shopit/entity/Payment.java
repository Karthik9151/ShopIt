package com.shopit.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
@Entity @Table(name="payments") @Getter @Setter @NoArgsConstructor
public class Payment {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="order_id",unique=true) private CustomerOrder order;
 @Column(nullable=false,length=30) private String provider;
 @Column(nullable=false,length=30) private String status;
 @Column(nullable=false,unique=true,length=100) private String reference;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal amount;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt=Instant.now();
 @Column(name="paid_at") private Instant paidAt;
}