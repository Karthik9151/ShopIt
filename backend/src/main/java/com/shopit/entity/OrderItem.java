package com.shopit.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
@Entity @Table(name="order_items") @Getter @Setter @NoArgsConstructor
public class OrderItem {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="order_id") private CustomerOrder order;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="product_id") private Product product;
 @Column(name="product_name",nullable=false,length=180) private String productName;
 @Column(name="unit_price",nullable=false,precision=12,scale=2) private BigDecimal unitPrice;
 @Column(nullable=false) private int quantity;
}