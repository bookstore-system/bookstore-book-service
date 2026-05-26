package com.notfound.bookservice.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Map claim role/scope từ JWT (monolith: scope=ADMIN; user-service: role=ROLE_ADMIN).
 */
public class JwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        addFromClaim(authorities, jwt.getClaim("scope"));
        addFromClaim(authorities, jwt.getClaim("role"));
        addFromClaim(authorities, jwt.getClaim("authorities"));

        if (authorities.isEmpty()) {
            return Collections.emptyList();
        }
        return authorities;
    }

    private void addFromClaim(List<GrantedAuthority> authorities, Object claim) {
        if (claim == null) {
            return;
        }
        if (claim instanceof String value) {
            addAuthority(authorities, value);
            return;
        }
        if (claim instanceof Collection<?> collection) {
            collection.forEach(item -> addAuthority(authorities, String.valueOf(item)));
        }
    }

    private void addAuthority(List<GrantedAuthority> authorities, String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            return;
        }
        String normalized = rawRole.trim().toUpperCase();
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(normalized);
        if (authorities.stream().noneMatch(a -> a.getAuthority().equals(authority.getAuthority()))) {
            authorities.add(authority);
        }
    }
}
