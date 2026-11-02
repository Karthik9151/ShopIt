package com.shopit.service;
import com.shopit.entity.AppUser;
import com.shopit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
@Component
public class AdminBootstrap implements ApplicationRunner{
 private final UserRepository users;private final PasswordEncoder encoder;private final String email,password;
 public AdminBootstrap(UserRepository u,PasswordEncoder e,@Value("${app.admin.email:}")String email,@Value("${app.admin.password:}")String password){users=u;encoder=e;this.email=email;this.password=password;}
 @Override public void run(ApplicationArguments args){if(email==null||email.isBlank()||password==null||password.length()<12)return;if(users.existsByEmailIgnoreCase(email))return;AppUser a=new AppUser();a.setName("ShopIt Administrator");a.setEmail(email.trim().toLowerCase());a.setPasswordHash(encoder.encode(password));a.setRole("ADMIN");a.setEnabled(true);users.save(a);}
}