package com.shopit.controller;

import com.shopit.dto.ApiDtos.*;
import com.shopit.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/coupons")
public class AdminCouponController {
    private final CouponService coupons;
    public AdminCouponController(CouponService coupons) { this.coupons = coupons; }

    @GetMapping("/paged")
    public Page<CouponView> paged(
            @RequestParam(defaultValue = "ACTIVE") String bucket,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid pagination");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return coupons.adminPage(bucket, pageable);
    }

    @GetMapping
    public List<CouponView> all() { return coupons.allForAdmin(); }

    @PostMapping
    public ResponseEntity<CouponView> create(@Valid @RequestBody CouponRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(coupons.create(request));
    }

    @PutMapping("/{id}")
    public CouponView update(@PathVariable Long id, @Valid @RequestBody CouponRequest request) {
        return coupons.update(id, request);
    }

    @PutMapping("/{id}/status")
    public CouponView status(@PathVariable Long id, @Valid @RequestBody CouponStatusRequest request) {
        return coupons.setActive(id, request.active());
    }
}
