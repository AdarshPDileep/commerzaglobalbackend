package com.example.CommerzaGlobalBackend.franchise.dto;

import java.util.Map;

public record FranchiseInUseResponse(boolean success, String code, String message, Map<String, Long> dependencies) {
}
