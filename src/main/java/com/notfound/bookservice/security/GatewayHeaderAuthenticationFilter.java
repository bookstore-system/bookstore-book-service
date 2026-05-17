package com.notfound.bookservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Khi request đi qua API Gateway, gateway đã validate JWT và forward role qua header.
 */
@Component
@Profile("!test")
public class GatewayHeaderAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER_USER_ROLE = "X-User-Role";
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_EMAIL = "X-User-Email";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String role = request.getHeader(HEADER_USER_ROLE);
            if (StringUtils.hasText(role)) {
                String authority = role.trim().toUpperCase();
                if (!authority.startsWith("ROLE_")) {
                    authority = "ROLE_" + authority;
                }
                String principal = StringUtils.hasText(request.getHeader(HEADER_USER_EMAIL))
                        ? request.getHeader(HEADER_USER_EMAIL)
                        : request.getHeader(HEADER_USER_ID);
                if (!StringUtils.hasText(principal)) {
                    principal = "gateway-user";
                }
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, List.of(new SimpleGrantedAuthority(authority)));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}
