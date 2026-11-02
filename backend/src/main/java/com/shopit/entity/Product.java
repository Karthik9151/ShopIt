package com.shopit.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
@Entity @Table(name="products") @Getter @Setter @NoArgsConstructor
public class Product {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="category_id") private Category category;
 @Column(nullable=false,length=180) private String name;
 @Column(columnDefinition="TEXT") private String description;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal price;
 @Column(name="cost_price",nullable=false,precision=12,scale=2) private BigDecimal costPrice=BigDecimal.ZERO;
 @Column(nullable=false) private int stock;
 @Column(name="image_url",length=1000) private String imageUrl;
 @Column(nullable=false) private boolean active=true;
 @Version private Long version;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt=Instant.now();
}