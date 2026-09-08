package com.example.CommerzaGlobalBackend.admin.dto;

public record AdminLogoutResponse(
        boolean success,
        String message
) {
    public static AdminLogoutResponse loggedOut() {
        return new AdminLogoutResponse(true, "Admin logout successful");
    }
}

