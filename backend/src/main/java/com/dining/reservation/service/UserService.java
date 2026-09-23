package com.dining.reservation.service;

import com.dining.reservation.domain.Role;
import com.dining.reservation.domain.User;
import com.dining.reservation.dto.UserDtos.CreateUserRequest;
import com.dining.reservation.dto.UserDtos.UpdateProfileRequest;
import com.dining.reservation.dto.UserDtos.UpdateUserRequest;
import com.dining.reservation.exception.ApiException;
import com.dining.reservation.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> ApiException.notFound("User not found"));
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }

    @Transactional
    public User updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getById(userId);
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        return userRepository.save(user);
    }

    @Transactional
    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw ApiException.conflict("Email already registered");
        }
        User user = new User();
        user.setEmail(request.email().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setRole(request.role() == null ? Role.CUSTOMER : request.role());
        user.setEnabled(true);
        user = userRepository.save(user);
        auditService.record("USER_CREATED", "User", user.getId(),
                "Created " + user.getRole() + " " + user.getEmail());
        return user;
    }

    @Transactional
    public User updateUser(Long id, UpdateUserRequest request) {
        User user = getById(id);
        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.enabled() != null) {
            user.setEnabled(request.enabled());
        }
        user = userRepository.save(user);
        auditService.record("USER_UPDATED", "User", user.getId(), "Updated user " + user.getEmail());
        return user;
    }

    @Transactional
    public User createOrReplaceManager(CreateUserRequest request) {
        List<User> managers = userRepository.findByRole(Role.MANAGER);
        User manager;
        if (managers.isEmpty()) {
            manager = createUser(new CreateUserRequest(
                    request.email(),
                    request.password(),
                    request.fullName(),
                    request.phone(),
                    Role.MANAGER
            ));
        } else {
            manager = managers.get(0);
            manager.setEmail(request.email().toLowerCase());
            manager.setFullName(request.fullName());
            manager.setPhone(request.phone());
            if (request.password() != null && !request.password().isBlank()) {
                manager.setPassword(passwordEncoder.encode(request.password()));
            }
            manager.setRole(Role.MANAGER);
            manager.setEnabled(true);
            manager = userRepository.save(manager);
            auditService.record("MANAGER_UPDATED", "User", manager.getId(), "Manager account updated");
        }
        return manager;
    }
}
