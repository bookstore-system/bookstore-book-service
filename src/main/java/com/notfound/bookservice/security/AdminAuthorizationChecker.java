package com.notfound.bookservice.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AdminAuthorizationChecker {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    public void requireAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .noneMatch(ROLE_ADMIN::equals)) {
            throw new AccessDeniedException("Bạn không có quyền quản trị để thực hiện thao tác này");
        }
    }
}
