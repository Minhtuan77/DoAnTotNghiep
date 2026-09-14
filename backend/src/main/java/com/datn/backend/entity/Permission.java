package com.datn.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Integer permissionId;

    @Column(
            name = "permission_code",
            nullable = false,
            unique = true,
            length = 100
    )
    private String permissionCode;

    @Column(name = "description", length = 255)
    private String description;
}
