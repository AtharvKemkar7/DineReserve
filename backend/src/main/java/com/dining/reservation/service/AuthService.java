package com.dining.reservation.service;

import com.dining.reservation.domain.Role;
import com.dining.reservation.domain.User;
import com.dining.reservation.dto.AuthDtos.AuthResponse;
import com.dining.reservation.dto.AuthDtos.LoginRequest;
import com.dining.reservation.dto.AuthDtos.RegisterRequest;
import com.dining.reservation.exception.ApiException;
import com.dining.reservation.repository.UserRepository;
import com.dining.reservation.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.auditService = auditService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw ApiException.conflict("Email already registered");
        }
        User user = new User();
        user.setEmail(request.email().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setRole(Role.CUSTOMER);
        user.setEnabled(true);
        user = userRepository.save(user);
        auditService.record("USER_REGISTERED", "User", user.getId(), "Customer registered: " + user.getEmail());
        return toAuth(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password()));
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));
        if (!user.isEnabled()) {
            throw ApiException.forbidden("Account is disabled");
        }
        return toAuth(user);
    }

    private AuthResponse toAuth(User user) {
        return new AuthResponse(
                jwtService.generateToken(user),
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole()
        );
    }
}
