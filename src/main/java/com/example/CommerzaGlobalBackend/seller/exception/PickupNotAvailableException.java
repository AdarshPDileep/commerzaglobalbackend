package com.example.CommerzaGlobalBackend.seller.exception;

public class PickupNotAvailableException extends RuntimeException {
    private final String code;

    public PickupNotAvailableException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
