package com.example.CommerzaGlobalBackend.geography.dto;

import java.util.Map;

public record GeographyInUseResponse(boolean success, String code, String message, Map<String, Long> dependencies) {
}
