package com.example.CommerzaGlobalBackend.franchise.exception;

import java.util.Map;

public class FranchiseInUseException extends RuntimeException {

    private final String code;
    private final Map<String, Long> dependencies;

    public FranchiseInUseException(String code, String message, Map<String, Long> dependencies) {
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
