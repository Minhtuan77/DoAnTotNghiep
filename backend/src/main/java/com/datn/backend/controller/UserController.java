package com.datn.backend.controller;

import com.datn.backend.dto.request.AddressRequest;
import com.datn.backend.dto.request.ChangePasswordRequest;
import com.datn.backend.dto.request.UpdateProfileRequest;
import com.datn.backend.dto.response.AddressResponse;
import com.datn.backend.dto.response.UserResponse;
import com.datn.backend.security.CustomUserDetails;
import com.datn.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserResponse> getMyProfile(
            @AuthenticationPrincipal
            CustomUserDetails userDetails
    ) {

        return ResponseEntity.ok(
                userService.getMyProfile(
                        userDetails.getUserId()
                )
        );
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateMyProfile(
            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @Valid
            @RequestBody
            UpdateProfileRequest request
    ) {

        return ResponseEntity.ok(
                userService.updateMyProfile(
                        userDetails.getUserId(),
                        request
                )
        );
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @Valid
            @RequestBody
            ChangePasswordRequest request
    ) {

        userService.changePassword(
                userDetails.getUserId(),
                request
        );

        return ResponseEntity.noContent()
                .build();
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressResponse>>
    getMyAddresses(
            @AuthenticationPrincipal
            CustomUserDetails userDetails
    ) {

        return ResponseEntity.ok(
                userService.getMyAddresses(
                        userDetails.getUserId()
                )
        );
    }

    @PostMapping("/addresses")
    public ResponseEntity<AddressResponse> addAddress(
            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @Valid
            @RequestBody
            AddressRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        userService.addAddress(
                                userDetails.getUserId(),
                                request
                        )
                );
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<AddressResponse>
    updateAddress(
            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @PathVariable
            Long addressId,

            @Valid
            @RequestBody
            AddressRequest request
    ) {

        return ResponseEntity.ok(
                userService.updateAddress(
                        userDetails.getUserId(),
                        addressId,
                        request
                )
        );
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @PathVariable
            Long addressId
    ) {

        userService.deleteAddress(
                userDetails.getUserId(),
                addressId
        );

        return ResponseEntity.noContent()
                .build();
    }

    @PutMapping("/addresses/{addressId}/default")
    public ResponseEntity<AddressResponse>
    setDefaultAddress(
            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @PathVariable
            Long addressId
    ) {

        return ResponseEntity.ok(
                userService.setDefaultAddress(
                        userDetails.getUserId(),
                        addressId
                )
        );
    }
}
