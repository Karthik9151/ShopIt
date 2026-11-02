package com.shopit.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
@Entity @Table(name="customer_orders",uniqueConstraints=@UniqueConstraint(columnNames={"user_id","request_id"})) @Getter @Setter @NoArgsConstructor
public class CustomerOrder {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id") private AppUser user;
 @Column(name="request_id",nullable=false,length=80) private String requestId;
 @Column(nullable=false,length=30) private String status="PENDING_PAYMENT";
 @Column(nullable=false,precision=12,scale=2) private BigDecimal subtotal;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal shipping;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal tax;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal total;
 @Column(name="shipping_address",nullable=false,length=1000) private String shippingAddress;
 @Column(name="coupon_id") private Long couponId;
 @Column(name="coupon_code",length=40) private String couponCode;
 @Column(name="discount_amount",nullable=false,precision=12,scale=2) private BigDecimal discountAmount=BigDecimal.ZERO;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt=Instant.now();
 @OneToMany(mappedBy="order",cascade=CascadeType.ALL,orphanRemoval=true) private List<OrderItem> items=new ArrayList<>();
 public void addItem(OrderItem item){items.add(item);item.setOrder(this);}
}