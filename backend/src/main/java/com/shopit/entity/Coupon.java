package com.shopit.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "coupons")
@Getter @Setter @NoArgsConstructor
public class Coupon {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 40) private String code;
    @Column(length = 240) private String description;
    @Column(name = "discount_type", nullable = false, length = 20) private String discountType;
    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2) private BigDecimal discountValue;
    @Column(name = "minimum_order_amount", nullable = false, precision = 12, scale = 2) private BigDecimal minimumOrderAmount = BigDecimal.ZERO;
    @Column(name = "maximum_discount", precision = 12, scale = 2) private BigDecimal maximumDiscount;
    @Column(name = "starts_at", nullable = false) private Instant startsAt;
    @Column(name = "ends_at", nullable = false) private Instant endsAt;
    @Column(name = "usage_limit") private Integer usageLimit;
    @Column(name = "usage_count", nullable = false) private int usageCount = 0;
    @Column(nullable = false) private boolean active = true;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
}
