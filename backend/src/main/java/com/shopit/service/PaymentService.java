package com.shopit.service;

import com.shopit.dto.ApiDtos.PaymentView;
import com.shopit.entity.AppUser;
import com.shopit.entity.CustomerOrder;
import com.shopit.entity.Payment;
import com.shopit.repository.OrderRepository;
import com.shopit.repository.PaymentRepository;
import com.shopit.repository.UserRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class PaymentService {
    private final PaymentRepository payments;
    private final OrderRepository orders;
    private final UserRepository users;
    private final OrderStatusHistoryService statusHistory;
    private final OrderStatusTransitions transitions;

    public PaymentService(
            PaymentRepository payments,
            OrderRepository orders,
            UserRepository users,
            OrderStatusHistoryService statusHistory,
            OrderStatusTransitions transitions) {
        this.payments = payments;
        this.orders = orders;
        this.users = users;
        this.statusHistory = statusHistory;
        this.transitions = transitions;
    }

    public PaymentView simulate(String email, Long orderId) {
        AppUser user = user(email);
        CustomerOrder order = orders.findByIdAndUser_Id(orderId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if ("CANCELLED".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cancelled orders cannot be paid");
        }

        Payment payment = payments.findByOrder_Id(orderId).orElseGet(() -> {
            Payment created = new Payment();
            created.setOrder(order);
            created.setProvider("SIMULATED");
            created.setStatus("PENDING");
            created.setReference("SIM-" + UUID.randomUUID());
            created.setAmount(order.getTotal());
            return payments.save(created);
        });

        if (!"SUCCESS".equals(payment.getStatus())) {
            if (!"PENDING_PAYMENT".equals(order.getStatus())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Only unpaid orders can be paid");
            }
            payment.setStatus("SUCCESS");
            payment.setPaidAt(Instant.now());
            payments.save(payment);
        }

        if ("PENDING_PAYMENT".equals(order.getStatus())) {
            transitions.validateTransition("PENDING_PAYMENT", "CONFIRMED", true);
            order.setStatus("CONFIRMED");
            statusHistory.record(order, "PENDING_PAYMENT", "CONFIRMED", "PAYMENT");
        }

        return view(payment);
    }

    @Transactional(readOnly = true)
    public PaymentView getMine(String email, Long id) {
        AppUser user = user(email);
        Payment payment = payments.findByIdAndOrder_User_Id(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        return view(payment);
    }

    private PaymentView view(Payment payment) {
        return new PaymentView(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getProvider(),
                payment.getStatus(),
                payment.getReference(),
                payment.getAmount(),
                payment.getPaidAt());
    }

    private AppUser user(String email) {
        return users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
