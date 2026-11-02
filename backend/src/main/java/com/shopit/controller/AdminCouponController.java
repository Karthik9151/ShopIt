package com.shopit.controller;

import com.shopit.dto.ApiDtos.*;
import com.shopit.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/coupons")
public class AdminCouponController {
    private final CouponService coupons;
    public AdminCouponController(CouponService coupons) { this.coupons = coupons; }

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
