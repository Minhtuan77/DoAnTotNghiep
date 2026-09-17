package com.datn.backend.security;

import com.datn.backend.entity.User;
import com.datn.backend.entity.enums.UserStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

// Lớp bọc User entity để Spring Security hiểu được.
// Không cho User implements UserDetails trực tiếp.
@Getter
public class CustomUserDetails implements UserDetails {

    private final Long userId;

    private final String email;

    private final String passwordHash;

    private final UserStatus status;

    private final String roleCode;

    private final Set<GrantedAuthority> authorities;

    public CustomUserDetails(User user) {

        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.status = user.getStatus();
        this.roleCode = user.getRole().getRoleCode();

        Set<GrantedAuthority> auths = new HashSet<>();

        // Authority theo role:
        // ROLE_CUSTOMER, ROLE_STAFF, ROLE_ADMIN
        auths.add(
                new SimpleGrantedAuthority("ROLE_" + roleCode)
        );

        // Authority theo permission chi tiết
        if (user.getRole().getPermissions() != null) {

            auths.addAll(
                    user.getRole()
                            .getPermissions()
                            .stream()
                            .map(p -> new SimpleGrantedAuthority(
                                    p.getPermissionCode()
                            ))
                            .collect(Collectors.toSet())
            );
        }

        this.authorities = auths;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.LOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}
