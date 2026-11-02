package com.shopit.entity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.Instant;
import java.util.*;
@Entity @Table(name="users") @Getter @Setter @NoArgsConstructor
public class AppUser implements UserDetails {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=120) private String name;
 @Column(nullable=false,unique=true,length=190) private String email;
 @Column(name="password_hash",nullable=false,length=100) private String passwordHash;
 @Column(length=30) private String phone;
 @Column(nullable=false,length=20) private String role="CUSTOMER";
 @Column(nullable=false) private boolean enabled=true;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt=Instant.now();
 @Override public Collection<? extends GrantedAuthority> getAuthorities(){return List.of(new SimpleGrantedAuthority("ROLE_"+role));}
 @Override public String getUsername(){return email;}
 @Override public String getPassword(){return passwordHash;}
 @Override public boolean isAccountNonExpired(){return true;}
 @Override public boolean isAccountNonLocked(){return true;}
 @Override public boolean isCredentialsNonExpired(){return true;}
}