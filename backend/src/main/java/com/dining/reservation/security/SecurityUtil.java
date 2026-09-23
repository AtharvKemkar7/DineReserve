package com.dining.reservation.security;

import com.dining.reservation.domain.Role;
import com.dining.reservation.exception.ApiException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {}

    public static UserPrincipal currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw ApiException.unauthorized("Authentication required");
        }
        return principal;
    }

    public static Long currentUserId() {
        return currentUser().getId();
    }

    public static boolean hasRole(Role role) {
        return currentUser().getRole() == role;
    }

    public static boolean isStaff() {
        Role role = currentUser().getRole();
        return role == Role.MANAGER || role == Role.ADMIN;
    }
}
