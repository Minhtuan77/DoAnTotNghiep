package com.datn.backend.service;

import com.datn.backend.dto.request.AddressRequest;
import com.datn.backend.dto.request.ChangePasswordRequest;
import com.datn.backend.dto.request.UpdateProfileRequest;
import com.datn.backend.dto.response.AddressResponse;
import com.datn.backend.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse getMyProfile(Long userId);

    UserResponse updateMyProfile(
            Long userId,
            UpdateProfileRequest request
    );

    void changePassword(
            Long userId,
            ChangePasswordRequest request
    );

    List<AddressResponse> getMyAddresses(Long userId);

    AddressResponse addAddress(
            Long userId,
            AddressRequest request
    );

    AddressResponse updateAddress(
            Long userId,
            Long addressId,
            AddressRequest request
    );

    void deleteAddress(
            Long userId,
            Long addressId
    );

    AddressResponse setDefaultAddress(
            Long userId,
            Long addressId
    );
}
