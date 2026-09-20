package com.datn.backend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignPermissionsRequest {

    @NotEmpty(message = "Danh sách permissionIds không được để trống")
    private List<Integer> permissionIds;
}
