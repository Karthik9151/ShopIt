package com.shopit.service;
import com.shopit.dto.ApiDtos.*;
import com.shopit.entity.AppUser;
import com.shopit.repository.UserRepository;
import com.shopit.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
@Service
public class AuthService{
 private final UserRepository users;private final PasswordEncoder encoder;private final AuthenticationManager manager;private final JwtService jwt;private final long expiry;
 public AuthService(UserRepository u,PasswordEncoder e,AuthenticationManager m,JwtService j,@Value("${app.jwt.expiration-ms}")long x){users=u;encoder=e;manager=m;jwt=j;expiry=x;}
 public AuthResponse register(RegisterRequest r){
  if(users.existsByEmailIgnoreCase(r.email()))throw new ResponseStatusException(HttpStatus.CONFLICT,"An account with this email already exists");
  AppUser u=new AppUser();u.setName(r.name().trim());u.setEmail(r.email().trim().toLowerCase());u.setPasswordHash(encoder.encode(r.password()));u.setPhone(r.phone());u.setRole("CUSTOMER");
  return response(users.save(u));
 }
 public AuthResponse login(LoginRequest r){
  try{manager.authenticate(new UsernamePasswordAuthenticationToken(r.email().trim().toLowerCase(),r.password()));}
  catch(AuthenticationException ex){throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password");}
  AppUser u=users.findByEmailIgnoreCase(r.email()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password"));
  return response(u);
 }
 private AuthResponse response(AppUser u){return new AuthResponse(jwt.generateToken(u),new UserView(u.getId(),u.getName(),u.getEmail(),u.getRole()),expiry);}
}