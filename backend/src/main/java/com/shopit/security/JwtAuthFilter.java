package com.shopit.security;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
@Component
public class JwtAuthFilter extends OncePerRequestFilter{
 private final JwtService jwt;private final UserDetailsService users;
 public JwtAuthFilter(JwtService jwt,UserDetailsService users){this.jwt=jwt;this.users=users;}
 @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)throws ServletException,IOException{
  String header=request.getHeader("Authorization");
  if(header!=null&&header.startsWith("Bearer ")){
   try{
    String token=header.substring(7),email=jwt.extractSubject(token);
    if(email!=null&&SecurityContextHolder.getContext().getAuthentication()==null){
     UserDetails user=users.loadUserByUsername(email);
     if(user.isEnabled()&&jwt.isValid(token,user.getUsername())){
      var auth=new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities());
      auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(auth);
     }
    }
   }catch(RuntimeException ex){SecurityContextHolder.clearContext();}
  }
  chain.doFilter(request,response);
 }
}