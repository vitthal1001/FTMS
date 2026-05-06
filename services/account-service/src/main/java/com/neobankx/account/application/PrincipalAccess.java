package com.neobankx.account.application;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public record PrincipalAccess(String subject, List<String> roles) {
    public static PrincipalAccess from(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return new PrincipalAccess(jwt.getSubject(), roles == null ? List.of() : roles);
    }

    public boolean isPrivileged() {
        return roles.contains("ADMIN") || roles.contains("SUPPORT");
    }

    public boolean isAdmin() {
        return roles.contains("ADMIN");
    }
}

