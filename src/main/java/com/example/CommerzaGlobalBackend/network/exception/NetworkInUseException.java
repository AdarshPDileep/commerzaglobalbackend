package com.example.CommerzaGlobalBackend.network.exception;

import java.util.Map;

public class NetworkInUseException extends RuntimeException {

    private final String code;
    private final Map<String, Long> dependencies;

    public NetworkInUseException(String code, String message, Map<String, Long> dependencies) {
        super(message);
        this.code = code;
        this.dependencies = dependencies;
    }

    public String getCode() {
        return code;
    }

    public Map<String, Long> getDependencies() {
        return dependencies;
    }
}
