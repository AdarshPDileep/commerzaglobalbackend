package com.example.CommerzaGlobalBackend.admin.controller;

import com.example.CommerzaGlobalBackend.admin.dto.AdminLoginRequest;
import com.example.CommerzaGlobalBackend.admin.dto.AdminLoginResponse;
import com.example.CommerzaGlobalBackend.admin.dto.AdminLogoutResponse;
import com.example.CommerzaGlobalBackend.admin.service.AdminAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(@RequestBody AdminLoginRequest request) {
        AdminLoginResponse response = adminAuthService.login(request);
        if (!response.success()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<AdminLogoutResponse> logout() {
        return ResponseEntity.ok(AdminLogoutResponse.loggedOut());
    }
}


