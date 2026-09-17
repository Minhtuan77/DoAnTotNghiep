package com.datn.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {

    private Long addressId;

    private String recipientName;

    private String phone;

    private String province;

    private String district;

    private String ward;

    private String detailAddress;

    private Boolean isDefault;
}