package com.dining.reservation.dto;

import com.dining.reservation.domain.Role;
import com.dining.reservation.domain.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class UserDtos {

    public record UserResponse(
            Long id,
            String email,
            String fullName,
            String phone,
            Role role,
            boolean enabled,
            Instant createdAt
    ) {
        public static UserResponse from(User user) {
            return new UserResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getPhone(),
                    user.getRole(),
                    user.isEnabled(),
                    user.getCreatedAt()
            );
        }
    }

    public record UpdateProfileRequest(
            @NotBlank @Size(min = 2, max = 100) String fullName,
            String phone
    ) {}

    public record CreateUserRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, max = 80) String password,
            @NotBlank @Size(min = 2, max = 100) String fullName,
            String phone,
            Role role
    ) {}

    public record UpdateUserRequest(
            String fullName,
            String phone,
            Role role,
            Boolean enabled
    ) {}
}
