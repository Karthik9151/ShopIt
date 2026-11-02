package com.shopit.security;
import com.shopit.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
@Service
public class ShopUserDetailsService implements UserDetailsService{
 private final UserRepository users;
 public ShopUserDetailsService(UserRepository users){this.users=users;}
 @Override public UserDetails loadUserByUsername(String email){return users.findByEmailIgnoreCase(email).orElseThrow(()->new UsernameNotFoundException("Account not found"));}
}