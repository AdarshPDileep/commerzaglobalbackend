package com.example.CommerzaGlobalBackend.common.exception;

import com.example.CommerzaGlobalBackend.common.ApiResponse;
import com.example.CommerzaGlobalBackend.franchise.dto.FranchiseInUseResponse;
import com.example.CommerzaGlobalBackend.franchise.exception.FranchiseInUseException;
import com.example.CommerzaGlobalBackend.geography.dto.GeographyInUseResponse;
import com.example.CommerzaGlobalBackend.geography.exception.GeographyInUseException;
import com.example.CommerzaGlobalBackend.network.dto.NetworkInUseResponse;
import com.example.CommerzaGlobalBackend.seller.dto.PickupUnavailableResponse;
import com.example.CommerzaGlobalBackend.seller.exception.PickupNotAvailableException;
import com.example.CommerzaGlobalBackend.network.exception.NetworkInUseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(GeographyInUseException.class)
    public ResponseEntity<GeographyInUseResponse> handleGeographyInUse(GeographyInUseException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new GeographyInUseResponse(false, ex.getCode(), ex.getMessage(), ex.getDependencies()));
    }
    @ExceptionHandler(NetworkInUseException.class)
    public ResponseEntity<NetworkInUseResponse> handleNetworkInUse(NetworkInUseException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new NetworkInUseResponse(false, ex.getCode(), ex.getMessage(), ex.getDependencies()));
    }

    @ExceptionHandler(FranchiseInUseException.class)
    public ResponseEntity<FranchiseInUseResponse> handleFranchiseInUse(FranchiseInUseException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new FranchiseInUseResponse(false, ex.getCode(), ex.getMessage(), ex.getDependencies()));
    }

    @ExceptionHandler(PickupNotAvailableException.class)
    public ResponseEntity<PickupUnavailableResponse> handlePickupNotAvailable(PickupNotAvailableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new PickupUnavailableResponse(false, ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler({DuplicateResourceException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.failure(ex.getMessage()));
    }
}






