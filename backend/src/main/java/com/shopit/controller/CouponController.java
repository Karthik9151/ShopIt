package com.shopit.controller;

import com.shopit.dto.ApiDtos.*;
import com.shopit.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {
    private final CouponService coupons;
    public CouponController(CouponService coupons) { this.coupons = coupons; }

    @GetMapping
    public List<CouponView> activeOffers() { return coupons.activeOffers(); }

    @PostMapping("/validate")
    public CouponPreview validate(Authentication authentication, @Valid @RequestBody CouponCheckRequest request) {
        return coupons.preview(authentication.getName(), request.code());
    }
}
