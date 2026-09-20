package com.datn.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class CreatePermissionRequest {

    @NotBlank(message = "permissionCode không được để trống")
    @Size(max = 100)
    private String permissionCode;

    @Size(max = 255)
    private String description;
}
