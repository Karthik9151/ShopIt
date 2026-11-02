package com.shopit.controller;
import com.shopit.dto.ApiDtos.PaymentView;
import com.shopit.service.PaymentService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/payments")
public class PaymentController{
 private final PaymentService payments;public PaymentController(PaymentService p){payments=p;}
 @PostMapping("/simulate/{orderId}") public PaymentView simulate(Authentication a,@PathVariable Long orderId){return payments.simulate(a.getName(),orderId);}
 @GetMapping("/{id}") public PaymentView get(Authentication a,@PathVariable Long id){return payments.getMine(a.getName(),id);}
}