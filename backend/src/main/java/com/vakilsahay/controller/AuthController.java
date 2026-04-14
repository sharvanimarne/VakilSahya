package com.vakilsahay.controller;

import com.vakilsahay.dto.request.LoginRequest;
import com.vakilsahay.dto.request.RegisterRequest;
import com.vakilsahay.dto.response.Responses.AuthResponse;
import com.vakilsahay.entity.User;
import com.vakilsahay.security.JwtUtil;
import com.vakilsahay.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController — public endpoints for registration and login.
 * POST /api/auth/register
 * POST /api/auth/login
 * © 2025 VakilSahay
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and login to get JWT token")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil     jwtUtil;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        User user  = authService.register(req.email(), req.fullName(), req.password());
        String jwt = jwtUtil.generateToken(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.of(jwt, user));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        User user  = authService.authenticate(req.email(), req.password());
        String jwt = jwtUtil.generateToken(user);
        return ResponseEntity.ok(AuthResponse.of(jwt, user));
    }
}