package com.shopit.service;
import com.shopit.dto.ApiDtos.PaymentView;
import com.shopit.entity.*;
import com.shopit.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.UUID;
@Service @Transactional
public class PaymentService{
 private final PaymentRepository payments;private final OrderRepository orders;private final UserRepository users;
 public PaymentService(PaymentRepository p,OrderRepository o,UserRepository u){payments=p;orders=o;users=u;}
 public PaymentView simulate(String email,Long orderId){AppUser u=user(email);CustomerOrder o=orders.findByIdAndUser_Id(orderId,u.getId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Order not found"));if("CANCELLED".equals(o.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Cancelled orders cannot be paid");Payment p=payments.findByOrder_Id(orderId).orElseGet(()->{Payment n=new Payment();n.setOrder(o);n.setProvider("SIMULATED");n.setStatus("PENDING");n.setReference("SIM-"+UUID.randomUUID());n.setAmount(o.getTotal());return payments.save(n);});if(!"SUCCESS".equals(p.getStatus())){p.setStatus("SUCCESS");p.setPaidAt(Instant.now());if("PENDING_PAYMENT".equals(o.getStatus()))o.setStatus("CONFIRMED");}return view(p);}
 @Transactional(readOnly=true) public PaymentView getMine(String email,Long id){AppUser u=user(email);Payment p=payments.findByIdAndOrder_User_Id(id,u.getId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Payment not found"));return view(p);}
 private PaymentView view(Payment p){return new PaymentView(p.getId(),p.getOrder().getId(),p.getProvider(),p.getStatus(),p.getReference(),p.getAmount(),p.getPaidAt());}
 private AppUser user(String email){return users.findByEmailIgnoreCase(email).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED));}
}