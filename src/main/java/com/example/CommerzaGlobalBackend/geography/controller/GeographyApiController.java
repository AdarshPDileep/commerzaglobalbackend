package com.example.CommerzaGlobalBackend.geography.controller;

import com.example.CommerzaGlobalBackend.common.ApiResponse;
import com.example.CommerzaGlobalBackend.geography.dto.ServiceabilityResponse;
import com.example.CommerzaGlobalBackend.geography.service.GeographyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/geography")
public class GeographyApiController {

    private final GeographyService geographyService;

    public GeographyApiController(GeographyService geographyService) {
        this.geographyService = geographyService;
    }

    @GetMapping("/serviceability/{pincode}")
    public ApiResponse<ServiceabilityResponse> checkServiceability(@PathVariable String pincode) {
        return ApiResponse.success(geographyService.checkServiceability(pincode));
    }
}
