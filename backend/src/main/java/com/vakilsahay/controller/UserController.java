package com.vakilsahay.controller;

import com.vakilsahay.dto.response.Responses.AdminStatsResponse;
import com.vakilsahay.entity.User;
import com.vakilsahay.repository.DocumentRepository;
import com.vakilsahay.repository.UsageLogRepository;
import com.vakilsahay.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * UserController — authenticated user profile.
 * AdminController — platform statistics (ADMIN role only).
 * © 2025 VakilSahay
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final AuthService        authService;
    private final DocumentRepository documentRepository;
    private final UsageLogRepository usageLogRepository;

    @GetMapping("/users/me")
    @Tag(name = "Users")
    @Operation(summary = "Get current user profile and usage stats")
    public ResponseEntity<Map<String, Object>> getMe(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(Map.of(
                "id",            currentUser.getId(),
                "email",         currentUser.getEmail(),
                "fullName",      currentUser.getFullName(),
                "role",          currentUser.getRole(),
                "createdAt",     currentUser.getCreatedAt(),
                "documentCount", documentRepository.countByUserId(currentUser.getId()),
                "totalActions",  usageLogRepository.countByUserId(currentUser.getId())
        ));
    }

    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Tag(name = "Admin")
    @Operation(summary = "Get platform-wide statistics (ADMIN only)")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(new AdminStatsResponse(
                authService.getTotalUsers(),
                documentRepository.countTotal(),
                usageLogRepository.count()
        ));
    }
}