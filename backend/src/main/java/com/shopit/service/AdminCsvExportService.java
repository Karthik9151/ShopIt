package com.shopit.service;

import com.shopit.entity.Category;
import com.shopit.entity.CustomerOrder;
import com.shopit.entity.Product;
import com.shopit.repository.OrderRepository;
import com.shopit.repository.ProductRepository;
import java.time.Instant;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class AdminCsvExportService {
    private static final int MAX_EXPORT_ROWS = 5000;
    private static final int FETCH_LIMIT = MAX_EXPORT_ROWS + 1;

    private final OrderRepository orders;
    private final ProductRepository products;

    public AdminCsvExportService(OrderRepository orders, ProductRepository products) {
        this.orders = orders;
        this.products = products;
    }

    public String ordersCsv(
            String status,
            Long orderId,
            String customerEmail,
            Instant createdFrom,
            Instant createdBefore) {
        validateDateRange(createdFrom, createdBefore);
        String normalizedStatus = status == null || status.isBlank()
                ? null : status.trim().toUpperCase(Locale.ROOT);
        String normalizedEmail = customerEmail == null || customerEmail.isBlank()
                ? null : customerEmail.trim().toLowerCase(Locale.ROOT);

        Page<CustomerOrder> page = orders.searchForAdmin(
                normalizedStatus,
                orderId,
                normalizedEmail,
                createdFrom,
                createdBefore,
                PageRequest.of(0, FETCH_LIMIT, Sort.by(Sort.Direction.DESC, "createdAt")));
        ensureWithinLimit(page.getTotalElements());

        StringBuilder csv = new StringBuilder();
        csv.append(CsvExportSupport.row(
                "Order ID",
                "Customer email",
                "Created at",
                "Status",
                "Subtotal",
                "Discount",
                "Shipping",
                "Tax",
                "Total"));

        for (CustomerOrder order : page.getContent()) {
            csv.append(CsvExportSupport.row(
                    order.getId(),
                    order.getUser() == null ? "" : order.getUser().getEmail(),
                    order.getCreatedAt(),
                    order.getStatus(),
                    order.getSubtotal(),
                    order.getDiscountAmount(),
                    order.getShipping(),
                    order.getTax(),
                    order.getTotal()));
        }
        return csv.toString();
    }

    public String inventoryCsv(Integer stockThreshold, String sortBy, String direction) {
        if (stockThreshold != null && (stockThreshold < 0 || stockThreshold > 1_000_000)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock threshold must be between 0 and 1,000,000");
        }
        String safeSort = switch (sortBy == null ? "createdAt" : sortBy) {
            case "name" -> "name";
            case "stock" -> "stock";
            case "price" -> "price";
            case "createdAt" -> "createdAt";
            default -> "createdAt";
        };
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Page<Product> page = products.searchInventory(
                stockThreshold,
                PageRequest.of(0, FETCH_LIMIT, Sort.by(sortDirection, safeSort)));
        ensureWithinLimit(page.getTotalElements());

        StringBuilder csv = new StringBuilder();
        csv.append(CsvExportSupport.row(
                "Product ID",
                "Product",
                "Category",
                "Stock",
                "Selling price",
                "Cost price",
                "Active"));

        for (Product product : page.getContent()) {
            Category category = product.getCategory();
            csv.append(CsvExportSupport.row(
                    product.getId(),
                    product.getName(),
                    category == null ? "" : category.getName(),
                    product.getStock(),
                    product.getPrice(),
                    product.getCostPrice(),
                    product.isActive()));
        }
        return csv.toString();
    }

    private void validateDateRange(Instant createdFrom, Instant createdBefore) {
        if (createdFrom != null && createdBefore != null && !createdBefore.isAfter(createdFrom)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The end date must be later than the start date");
        }
    }

    private void ensureWithinLimit(long rowCount) {
        if (rowCount > MAX_EXPORT_ROWS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Export contains more than 5,000 rows; narrow the filters and try again");
        }
    }
}
