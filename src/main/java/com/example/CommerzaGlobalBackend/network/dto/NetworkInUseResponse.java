package com.example.CommerzaGlobalBackend.network.dto;

import java.util.Map;

public record NetworkInUseResponse(boolean success, String code, String message, Map<String, Long> dependencies) {
}
