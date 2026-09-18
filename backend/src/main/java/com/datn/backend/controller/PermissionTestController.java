package com.datn.backend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test/permissions")
public class PermissionTestController {

    @GetMapping("/read")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public String read() {
        return "Bạn có quyền PRODUCT_READ";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    public String create() {
        return "Bạn có quyền PRODUCT_CREATE";
    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    public String update() {
        return "Bạn có quyền PRODUCT_UPDATE";
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE')")
    public String delete() {
        return "Bạn có quyền PRODUCT_DELETE";
    }
}
