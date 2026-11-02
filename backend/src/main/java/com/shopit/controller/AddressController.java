package com.shopit.controller;
import com.shopit.dto.ApiDtos.*;
import com.shopit.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {
    private final AddressService addresses;
    public AddressController(AddressService addresses) { this.addresses = addresses; }

    @GetMapping
    public List<AddressView> list(Authentication authentication) { return addresses.list(authentication.getName()); }

    @PostMapping
    public ResponseEntity<AddressView> create(Authentication authentication, @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addresses.create(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    public AddressView update(Authentication authentication, @PathVariable Long id,
                              @Valid @RequestBody AddressRequest request) {
        return addresses.update(authentication.getName(), id, request);
    }

    @PutMapping("/{id}/default")
    public AddressView makeDefault(Authentication authentication, @PathVariable Long id) {
        return addresses.makeDefault(authentication.getName(), id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        addresses.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
