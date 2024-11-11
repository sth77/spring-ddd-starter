package com.example.app.common.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public enum UserRole {
    USER,
    ADMIN;

    String asAuthority() {
        return "ROLE_" + name();
    }

    public static Optional<UserRole> forAuthority(String authority) {
        return Stream.of(UserRole.values())
            .filter(it -> it.asAuthority().equals(authority))
            .findFirst();
    }

    public static List<UserRole> current() {
        return SecurityContextHolder.getContext()
            .getAuthentication()
            .getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .map(UserRole::forAuthority)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

    }
}
