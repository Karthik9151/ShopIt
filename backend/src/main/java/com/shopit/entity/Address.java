package com.shopit.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "addresses")
@Getter @Setter @NoArgsConstructor
public class Address {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
    @Column(nullable = false, length = 60) private String label;
    @Column(name = "recipient_name", nullable = false, length = 120) private String recipientName;
    @Column(nullable = false, length = 30) private String phone;
    @Column(name = "address_line1", nullable = false, length = 240) private String addressLine1;
    @Column(name = "address_line2", length = 240) private String addressLine2;
    @Column(nullable = false, length = 100) private String city;
    @Column(nullable = false, length = 100) private String state;
    @Column(name = "postal_code", nullable = false, length = 20) private String postalCode;
    @Column(nullable = false, length = 80) private String country;
    @Column(name = "is_default", nullable = false) private boolean defaultAddress;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
}
