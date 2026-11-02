package com.shopit.security;
import com.shopit.entity.AppUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;
@Service
public class JwtService {
 private final SecretKey key; private final long expiry;
 public JwtService(@Value("${app.jwt.secret-base64}")String secret,@Value("${app.jwt.expiration-ms}")long expiry){
  byte[] bytes=Decoders.BASE64.decode(secret);
  if(bytes.length<32)throw new IllegalArgumentException("JWT secret must contain at least 256 bits");
  key=Keys.hmacShaKeyFor(bytes);this.expiry=expiry;
 }
 public String generateToken(AppUser user){Date now=new Date();return Jwts.builder().subject(user.getEmail()).claim("role",user.getRole()).issuedAt(now).expiration(new Date(now.getTime()+expiry)).signWith(key).compact();}
 public String extractSubject(String token){return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();}
 public boolean isValid(String token,String email){try{return email.equals(extractSubject(token));}catch(JwtException|IllegalArgumentException ex){return false;}}
}