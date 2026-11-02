package com.shopit.service;
import com.shopit.dto.ApiDtos.*;
import com.shopit.entity.*;
import com.shopit.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.*;
import java.util.*;
@Service @Transactional
public class OrderService{
 private static final BigDecimal SHIPPING=new BigDecimal("49.00"),TAX_RATE=new BigDecimal("0.18");
 private final OrderRepository orders;private final CartItemRepository cart;private final UserRepository users;private final ProductRepository products;
 public OrderService(OrderRepository o,CartItemRepository c,UserRepository u,ProductRepository p){orders=o;cart=c;users=u;products=p;}
 public OrderView checkout(String email,CheckoutRequest r){
  AppUser u=user(email);Optional<CustomerOrder> previous=orders.findByUser_IdAndRequestId(u.getId(),r.requestId());if(previous.isPresent())return view(previous.get());
  List<CartItem> lines=cart.findByUser_Id(u.getId());if(lines.isEmpty())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Your cart is empty");
  CustomerOrder o=new CustomerOrder();o.setUser(u);o.setRequestId(r.requestId());o.setStatus("PENDING_PAYMENT");o.setShippingAddress(r.shippingAddress().trim());BigDecimal subtotal=BigDecimal.ZERO;
  for(CartItem line:lines){Product p=products.findByIdForUpdate(line.getProduct().getId()).orElseThrow(()->new ResponseStatusException(HttpStatus.CONFLICT,"Product unavailable"));if(!p.isActive()||line.getQuantity()>p.getStock())throw new ResponseStatusException(HttpStatus.CONFLICT,"Stock changed for "+p.getName());p.setStock(p.getStock()-line.getQuantity());subtotal=subtotal.add(p.getPrice().multiply(BigDecimal.valueOf(line.getQuantity())));OrderItem oi=new OrderItem();oi.setProduct(p);oi.setProductName(p.getName());oi.setUnitPrice(p.getPrice());oi.setQuantity(line.getQuantity());o.addItem(oi);}
  BigDecimal tax=subtotal.multiply(TAX_RATE).setScale(2,RoundingMode.HALF_UP);o.setSubtotal(subtotal);o.setShipping(SHIPPING);o.setTax(tax);o.setTotal(subtotal.add(SHIPPING).add(tax).setScale(2,RoundingMode.HALF_UP));CustomerOrder saved=orders.save(o);cart.deleteByUser_Id(u.getId());return view(saved);
 }
 @Transactional(readOnly=true) public List<OrderView> mine(String email){AppUser u=user(email);return orders.findByUser_IdOrderByCreatedAtDesc(u.getId()).stream().map(this::view).toList();}
 @Transactional(readOnly=true) public OrderView getMine(String email,Long id){AppUser u=user(email);return view(orders.findByIdAndUser_Id(id,u.getId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Order not found")));}
 public OrderView cancel(String email,Long id){AppUser u=user(email);CustomerOrder o=orders.findByIdAndUser_Id(id,u.getId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Order not found"));if(!"PENDING_PAYMENT".equals(o.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Only unpaid orders can be cancelled by the customer");o.setStatus("CANCELLED");for(OrderItem i:o.getItems()){if(i.getProduct()!=null){Product p=products.findByIdForUpdate(i.getProduct().getId()).orElse(null);if(p!=null)p.setStock(p.getStock()+i.getQuantity());}}return view(o);}
 @Transactional(readOnly=true) public List<OrderView> all(){return orders.findAllByOrderByCreatedAtDesc().stream().map(this::view).toList();}
 public OrderView updateStatus(Long id,String next){Set<String> valid=Set.of("CONFIRMED","PROCESSING","SHIPPED","DELIVERED","CANCELLED");if(!valid.contains(next))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unsupported order status");CustomerOrder o=orders.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));if(Set.of("CANCELLED","DELIVERED").contains(o.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Order is final");if("CANCELLED".equals(next)&&!"PENDING_PAYMENT".equals(o.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Refund handling is required before cancellation");o.setStatus(next);return view(o);}
 public OrderView view(CustomerOrder o){List<OrderLine> lines=o.getItems().stream().map(i->new OrderLine(i.getProduct()==null?null:i.getProduct().getId(),i.getProductName(),i.getUnitPrice(),i.getQuantity(),i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))).toList();return new OrderView(o.getId(),o.getStatus(),o.getSubtotal(),o.getShipping(),o.getTax(),o.getTotal(),o.getShippingAddress(),o.getCreatedAt(),lines);}
 private AppUser user(String email){return users.findByEmailIgnoreCase(email).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED));}
}