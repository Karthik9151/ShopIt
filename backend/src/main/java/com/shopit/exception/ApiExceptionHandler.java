package com.shopit.exception;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.*;
@RestControllerAdvice
public class ApiExceptionHandler{
 public record ApiError(Instant timestamp,int status,String error,String message,Map<String,String> fields){}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ApiError> validation(MethodArgumentNotValidException e){Map<String,String> f=new LinkedHashMap<>();e.getBindingResult().getFieldErrors().forEach(x->f.putIfAbsent(x.getField(),x.getDefaultMessage()));return ResponseEntity.badRequest().body(new ApiError(Instant.now(),400,"Bad Request","Validation failed",f));}
 @ExceptionHandler(ResponseStatusException.class) ResponseEntity<ApiError> status(ResponseStatusException e){int n=e.getStatusCode().value();return ResponseEntity.status(e.getStatusCode()).body(new ApiError(Instant.now(),n,e.getStatusCode().toString(),e.getReason()==null?"Request failed":e.getReason(),Map.of()));}
 @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<ApiError> bad(IllegalArgumentException e){return ResponseEntity.badRequest().body(new ApiError(Instant.now(),400,"Bad Request",e.getMessage(),Map.of()));}
 @ExceptionHandler(Exception.class) ResponseEntity<ApiError> internal(Exception e){return ResponseEntity.status(500).body(new ApiError(Instant.now(),500,"Internal Server Error","The request could not be completed",Map.of()));}
}