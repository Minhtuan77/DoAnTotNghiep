package com.datn.backend.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "Authorization";

    private static final String TOKEN_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader =
                request.getHeader(HEADER_NAME);

        if (authHeader == null
                || !authHeader.startsWith(TOKEN_PREFIX)) {

            filterChain.doFilter(request, response);
            return;
        }

        String token =
                authHeader.substring(TOKEN_PREFIX.length());

        try {

            String tokenType =
                    jwtUtil.extractTokenType(token);

            // Chỉ Access Token được dùng để
            // authenticate request tới API
            if (!"ACCESS".equals(tokenType)) {

                filterChain.doFilter(request, response);
                return;
            }

            String email =
                    jwtUtil.extractEmail(token);

            if (email != null
                    && SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(email);

                // Trạng thái tài khoản luôn được lấy MỚI từ DB ở mỗi request
                // (không đọc từ claim trong JWT), nhưng userDetails ở trên chỉ
                // mới được TẢI, chưa được KIỂM TRA. Nếu bỏ qua bước dưới đây,
                // một tài khoản vừa bị Admin khóa (LOCKED/UNVERIFIED) vẫn có
                // thể tiếp tục gọi API bình thường cho tới khi access token
                // hết hạn (tối đa 15 phút), vì access token là JWT stateless,
                // không tra cứu session đã bị revoke trong DB.
                if (!userDetails.isEnabled()
                        || !userDetails.isAccountNonLocked()) {

                    filterChain.doFilter(request, response);
                    return;
                }

                if (jwtUtil.isTokenValid(
                        token,
                        userDetails.getUsername()
                )) {

                    UsernamePasswordAuthenticationToken
                            authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authToken);
                }
            }

        } catch (JwtException
                 | IllegalArgumentException ex) {

            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}