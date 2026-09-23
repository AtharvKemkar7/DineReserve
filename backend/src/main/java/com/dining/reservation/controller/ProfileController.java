package com.dining.reservation.controller;

import com.dining.reservation.dto.UserDtos.UpdateProfileRequest;
import com.dining.reservation.dto.UserDtos.UserResponse;
import com.dining.reservation.security.SecurityUtil;
import com.dining.reservation.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public UserResponse me() {
        return UserResponse.from(userService.getById(SecurityUtil.currentUserId()));
    }

    @PutMapping
    public UserResponse update(@Valid @RequestBody UpdateProfileRequest request) {
        return UserResponse.from(userService.updateProfile(SecurityUtil.currentUserId(), request));
    }
}
