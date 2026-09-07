package com.example.CommerzaGlobalBackend.admin.dto;

public record AdminLoginResponse(
        boolean success,
        String message,
        String email,
        String role,
        String token
) {
    public static AdminLoginResponse success(String email, String token) {
        return new AdminLoginResponse(true, "Admin login successful", email, "ADMIN", token);
    }

    public static AdminLoginResponse failure() {
        return new AdminLoginResponse(false, "Invalid admin credentials", null, null, null);
    }
}
