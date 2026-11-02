package com.shopit.security;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import java.util.*;
@Configuration @EnableWebSecurity @EnableMethodSecurity
public class SecurityConfig{
 @Bean SecurityFilterChain filterChain(HttpSecurity http,JwtAuthFilter jwtFilter,RateLimitFilter rateLimitFilter,ApiSurfaceFilter apiSurfaceFilter,AuthenticationProvider provider)throws Exception{
  return http.csrf(csrf->csrf.disable()).cors(Customizer.withDefaults())
   .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authenticationProvider(provider)
   .authorizeHttpRequests(a->a.requestMatchers("/api/auth/**","/api/products/**","/api/categories/**","/api/coupons").permitAll()
    .requestMatchers("/actuator/health/**","/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html").permitAll()
    .requestMatchers("/api/admin/**").hasRole("ADMIN").anyRequest().authenticated())
   .exceptionHandling(e->e.authenticationEntryPoint((req,res,ex)->res.sendError(401,"Authentication required")).accessDeniedHandler((req,res,ex)->res.sendError(403,"Access denied")))
   .addFilterBefore(jwtFilter,UsernamePasswordAuthenticationFilter.class)
   .addFilterAfter(rateLimitFilter,JwtAuthFilter.class)
   .addFilterAfter(apiSurfaceFilter,RateLimitFilter.class).build();
 }

 @Bean RateLimitFilter rateLimitFilter(
  RateLimitService limits,
  @Value("${app.rate-limit.enabled:true}") boolean enabled,
  @Value("${app.rate-limit.trust-forwarded-for:false}") boolean trustForwardedFor) {
  return new RateLimitFilter(limits, enabled, trustForwardedFor);
 }
 @Bean FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(RateLimitFilter filter) {
  FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>(filter);
  registration.setEnabled(false);
  return registration;
 }

 @Bean ApiSurfaceFilter apiSurfaceFilter(
  @Value("${app.api-docs.enabled:false}") boolean apiDocsEnabled) {
  return new ApiSurfaceFilter(apiDocsEnabled);
 }
 @Bean FilterRegistrationBean<ApiSurfaceFilter> apiSurfaceFilterRegistration(ApiSurfaceFilter filter) {
  FilterRegistrationBean<ApiSurfaceFilter> registration = new FilterRegistrationBean<>(filter);
  registration.setEnabled(false);
  return registration;
 }
 @Bean AuthenticationProvider authenticationProvider(ShopUserDetailsService users,PasswordEncoder encoder){DaoAuthenticationProvider p=new DaoAuthenticationProvider();p.setUserDetailsService(users);p.setPasswordEncoder(encoder);return p;}
 @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration c)throws Exception{return c.getAuthenticationManager();}
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder(12);}
 @Bean CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origins}")String origins){
  CorsConfiguration c=new CorsConfiguration();c.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).filter(s->!s.isBlank()).toList());
  c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));c.setAllowedHeaders(List.of("Authorization","Content-Type","Idempotency-Key"));c.setAllowCredentials(false);
  UrlBasedCorsConfigurationSource s=new UrlBasedCorsConfigurationSource();s.registerCorsConfiguration("/**",c);return s;
 }
}