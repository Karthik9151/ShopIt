package com.shopit.service;

import com.shopit.dto.ApiDtos.OrderStatusHistoryView;
import com.shopit.entity.CustomerOrder;
import com.shopit.entity.OrderStatusHistory;
import com.shopit.repository.OrderStatusHistoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderStatusHistoryService {
    private final OrderStatusHistoryRepository history;

    public OrderStatusHistoryService(OrderStatusHistoryRepository history) {
        this.history = history;
    }

    public void record(CustomerOrder order, String oldStatus, String newStatus, String actor) {
        OrderStatusHistory entry = new OrderStatusHistory();
        entry.setOrder(order);
        entry.setOldStatus(oldStatus);
        entry.setNewStatus(newStatus);
        entry.setActor(actor);
        history.save(entry);
    }

    @Transactional(readOnly = true)
    public List<OrderStatusHistoryView> timeline(Long orderId) {
        return history.findByOrder_IdOrderByChangedAtAsc(orderId).stream()
                .map(entry -> new OrderStatusHistoryView(
                        entry.getOldStatus(),
                        entry.getNewStatus(),
                        entry.getChangedAt(),
                        entry.getActor()))
                .toList();
    }
}
