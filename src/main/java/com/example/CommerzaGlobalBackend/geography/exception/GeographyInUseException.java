package com.example.CommerzaGlobalBackend.geography.exception;

import java.util.Map;

public class GeographyInUseException extends RuntimeException {

    private final String code;
    private final Map<String, Long> dependencies;

    public GeographyInUseException(String message, Map<String, Long> dependencies) {
        super(message);
        this.code = "GEOGRAPHY_IN_USE";
        this.dependencies = dependencies;
    }

    public String getCode() {
        return code;
    }

    public Map<String, Long> getDependencies() {
        return dependencies;
    }
}
