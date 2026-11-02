package com.shopit.service;
import com.shopit.dto.ApiDtos.*;
import com.shopit.entity.Address;
import com.shopit.entity.AppUser;
import com.shopit.repository.AddressRepository;
import com.shopit.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@Transactional
public class AddressService {
    private final AddressRepository addresses;
    private final UserRepository users;

    public AddressService(AddressRepository addresses, UserRepository users) {
        this.addresses = addresses;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<AddressView> list(String email) {
        AppUser user = user(email);
        return addresses.findByUser_IdOrderByDefaultAddressDescCreatedAtDesc(user.getId())
            .stream().map(this::view).toList();
    }

    public AddressView create(String email, AddressRequest request) {
        AppUser user = user(email);
        Address address = new Address();
        address.setUser(user);
        apply(address, request);
        boolean makeDefault = request.defaultAddress() || addresses.countByUser_Id(user.getId()) == 0;
        if (makeDefault) clearDefault(user.getId());
        address.setDefaultAddress(makeDefault);
        return view(addresses.save(address));
    }

    public AddressView update(String email, Long id, AddressRequest request) {
        AppUser user = user(email);
        Address address = owned(id, user.getId());
        apply(address, request);
        if (request.defaultAddress()) {
            clearDefault(user.getId());
            address.setDefaultAddress(true);
        }
        return view(addresses.save(address));
    }

    public AddressView makeDefault(String email, Long id) {
        AppUser user = user(email);
        Address address = owned(id, user.getId());
        clearDefault(user.getId());
        address.setDefaultAddress(true);
        return view(addresses.save(address));
    }

    public void delete(String email, Long id) {
        AppUser user = user(email);
        Address address = owned(id, user.getId());
        boolean wasDefault = address.isDefaultAddress();
        addresses.delete(address);
        if (wasDefault) {
            List<Address> remaining = addresses.findByUser_IdOrderByDefaultAddressDescCreatedAtDesc(user.getId());
            if (!remaining.isEmpty()) {
                remaining.get(0).setDefaultAddress(true);
            }
        }
    }

    private void apply(Address address, AddressRequest request) {
        address.setLabel(request.label().trim());
        address.setRecipientName(request.recipientName().trim());
        address.setPhone(request.phone().trim());
        address.setAddressLine1(request.addressLine1().trim());
        address.setAddressLine2(request.addressLine2() == null ? null : request.addressLine2().trim());
        address.setCity(request.city().trim());
        address.setState(request.state().trim());
        address.setPostalCode(request.postalCode().trim());
        address.setCountry(request.country().trim());
    }

    private void clearDefault(Long userId) {
        addresses.findByUser_IdOrderByDefaultAddressDescCreatedAtDesc(userId)
            .forEach(address -> address.setDefaultAddress(false));
    }

    private Address owned(Long id, Long userId) {
        return addresses.findByIdAndUser_Id(id, userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));
    }

    private AddressView view(Address a) {
        return new AddressView(a.getId(), a.getLabel(), a.getRecipientName(), a.getPhone(),
            a.getAddressLine1(), a.getAddressLine2(), a.getCity(), a.getState(),
            a.getPostalCode(), a.getCountry(), a.isDefaultAddress());
    }

    private AppUser user(String email) {
        return users.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
