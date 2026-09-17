package com.datn.backend.service.impl;

import com.datn.backend.dto.request.AddressRequest;
import com.datn.backend.dto.request.ChangePasswordRequest;
import com.datn.backend.dto.request.UpdateProfileRequest;
import com.datn.backend.dto.response.AddressResponse;
import com.datn.backend.dto.response.UserResponse;
import com.datn.backend.entity.User;
import com.datn.backend.entity.UserAddress;
import com.datn.backend.exception.EmailAlreadyExistsException;
import com.datn.backend.exception.InvalidCredentialsException;
import com.datn.backend.exception.PhoneAlreadyExistsException;
import com.datn.backend.exception.ResourceNotFoundException;
import com.datn.backend.repository.UserAddressRepository;
import com.datn.backend.repository.UserRepository;
import com.datn.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserAddressRepository userAddressRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getMyProfile(
            Long userId
    ) {

        User user =
                findUser(userId);

        return toUserResponse(user);
    }

    @Override
    public UserResponse updateMyProfile(
            Long userId,
            UpdateProfileRequest request
    ) {

        User user =
                findUser(userId);

        if (request.getPhone() != null
                && !request.getPhone().isBlank()) {

            String phone =
                    request.getPhone().trim();

            if (!phone.equals(user.getPhone())
                    && userRepository.existsByPhone(phone)) {

                throw new PhoneAlreadyExistsException(
                        phone
                );
            }

            user.setPhone(phone);
        }

        if (request.getFullName() != null
                && !request.getFullName().isBlank()) {

            user.setFullName(
                    request.getFullName().trim()
            );
        }

        if (request.getAvatarUrl() != null) {

            user.setAvatarUrl(
                    request.getAvatarUrl()
            );
        }

        userRepository.save(user);

        return toUserResponse(user);
    }

    @Override
    public void changePassword(
            Long userId,
            ChangePasswordRequest request
    ) {

        User user =
                findUser(userId);

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPasswordHash()
        )) {

            throw new InvalidCredentialsException();
        }

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getMyAddresses(
            Long userId
    ) {

        findUser(userId);

        return userAddressRepository
                .findByUser_UserIdOrderByIsDefaultDesc(
                        userId
                )
                .stream()
                .map(this::toAddressResponse)
                .toList();
    }

    @Override
    public AddressResponse addAddress(
            Long userId,
            AddressRequest request
    ) {

        User user =
                findUser(userId);

        boolean shouldBeDefault =
                Boolean.TRUE.equals(
                        request.getIsDefault()
                );

        List<UserAddress> existing =
                userAddressRepository
                        .findByUser_UserIdOrderByIsDefaultDesc(
                                userId
                        );

        // Địa chỉ đầu tiên tự động default
        if (existing.isEmpty()) {
            shouldBeDefault = true;
        }

        if (shouldBeDefault) {

            unsetDefaultAddresses(
                    existing
            );
        }

        UserAddress address =
                UserAddress.builder()
                        .user(user)
                        .recipientName(
                                request.getRecipientName()
                        )
                        .phone(
                                request.getPhone()
                        )
                        .province(
                                request.getProvince()
                        )
                        .district(
                                request.getDistrict()
                        )
                        .ward(
                                request.getWard()
                        )
                        .detailAddress(
                                request.getDetailAddress()
                        )
                        .isDefault(
                                shouldBeDefault
                        )
                        .build();

        address =
                userAddressRepository.save(
                        address
                );

        return toAddressResponse(address);
    }

    @Override
    public AddressResponse updateAddress(
            Long userId,
            Long addressId,
            AddressRequest request
    ) {

        UserAddress address =
                findAddress(
                        userId,
                        addressId
                );

        List<UserAddress> existing =
                userAddressRepository
                        .findByUser_UserIdOrderByIsDefaultDesc(
                                userId
                        );

        boolean shouldBeDefault =
                Boolean.TRUE.equals(
                        request.getIsDefault()
                );

        if (shouldBeDefault) {

            unsetDefaultAddresses(
                    existing,
                    addressId
            );
        }

        address.setRecipientName(
                request.getRecipientName()
        );

        address.setPhone(
                request.getPhone()
        );

        address.setProvince(
                request.getProvince()
        );

        address.setDistrict(
                request.getDistrict()
        );

        address.setWard(
                request.getWard()
        );

        address.setDetailAddress(
                request.getDetailAddress()
        );

        address.setIsDefault(
                shouldBeDefault
        );

        address =
                userAddressRepository.save(
                        address
                );

        return toAddressResponse(address);
    }

    @Override
    public void deleteAddress(
            Long userId,
            Long addressId
    ) {

        UserAddress address =
                findAddress(
                        userId,
                        addressId
                );

        boolean wasDefault =
                Boolean.TRUE.equals(
                        address.getIsDefault()
                );

        userAddressRepository.delete(
                address
        );

        if (wasDefault) {

            List<UserAddress> remaining =
                    userAddressRepository
                            .findByUser_UserIdOrderByIsDefaultDesc(
                                    userId
                            );

            if (!remaining.isEmpty()) {

                UserAddress newDefault =
                        remaining.get(0);

                newDefault.setIsDefault(true);

                userAddressRepository.save(
                        newDefault
                );
            }
        }
    }

    @Override
    public AddressResponse setDefaultAddress(
            Long userId,
            Long addressId
    ) {

        UserAddress address =
                findAddress(
                        userId,
                        addressId
                );

        List<UserAddress> addresses =
                userAddressRepository
                        .findByUser_UserIdOrderByIsDefaultDesc(
                                userId
                        );

        unsetDefaultAddresses(
                addresses
        );

        address.setIsDefault(true);

        address =
                userAddressRepository.save(
                        address
                );

        return toAddressResponse(address);
    }

    private User findUser(
            Long userId
    ) {

        return userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy người dùng với ID: "
                                        + userId
                        )
                );
    }

    private UserAddress findAddress(
            Long userId,
            Long addressId
    ) {

        return userAddressRepository
                .findByAddressIdAndUser_UserId(
                        addressId,
                        userId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy địa chỉ"
                        )
                );
    }

    private void unsetDefaultAddresses(
            List<UserAddress> addresses
    ) {

        unsetDefaultAddresses(
                addresses,
                null
        );
    }

    private void unsetDefaultAddresses(
            List<UserAddress> addresses,
            Long exceptAddressId
    ) {

        for (UserAddress address : addresses) {

            if (exceptAddressId == null
                    || !address.getAddressId()
                    .equals(exceptAddressId)) {

                address.setIsDefault(false);
            }
        }

        userAddressRepository.saveAll(
                addresses
        );
    }

    private UserResponse toUserResponse(
            User user
    ) {

        return UserResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .roleCode(
                        user.getRole()
                                .getRoleCode()
                )
                .roleName(
                        user.getRole()
                                .getRoleName()
                )
                .status(
                        user.getStatus()
                                .name()
                )
                .createdAt(
                        user.getCreatedAt()
                )
                .build();
    }

    private AddressResponse toAddressResponse(
            UserAddress address
    ) {

        return AddressResponse.builder()
                .addressId(address.getAddressId())
                .recipientName(
                        address.getRecipientName()
                )
                .phone(
                        address.getPhone()
                )
                .province(
                        address.getProvince()
                )
                .district(
                        address.getDistrict()
                )
                .ward(
                        address.getWard()
                )
                .detailAddress(
                        address.getDetailAddress()
                )
                .isDefault(
                        address.getIsDefault()
                )
                .build();
    }
}
