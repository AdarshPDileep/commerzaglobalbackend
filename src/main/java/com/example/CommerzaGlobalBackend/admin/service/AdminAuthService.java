package com.example.CommerzaGlobalBackend.admin.service;

import com.example.CommerzaGlobalBackend.admin.dto.AdminLoginRequest;
import com.example.CommerzaGlobalBackend.admin.dto.AdminLoginResponse;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

@Service
public class AdminAuthService {

    private static final String ADMIN_EMAIL = "admin@commerzaglobal.local";
    private static final String ADMIN_PASSWORD = "passowrd";

    public AdminLoginResponse login(AdminLoginRequest request) {
        if (!isValidCredential(request)) {
            return AdminLoginResponse.failure();
        }

        return AdminLoginResponse.success(ADMIN_EMAIL, createAdminToken());
    }

    private boolean isValidCredential(AdminLoginRequest request) {
        if (request == null) {
            return false;
        }

        return secureEquals(ADMIN_EMAIL, request.email())
                && secureEquals(ADMIN_PASSWORD, request.password());
    }

    private boolean secureEquals(String expected, String actual) {
        if (actual == null) {
            return false;
        }

        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String createAdminToken() {
        String tokenPayload = ADMIN_EMAIL + ":ADMIN:" + Instant.now().toEpochMilli();
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenPayload.getBytes(StandardCharsets.UTF_8));
    }
}
