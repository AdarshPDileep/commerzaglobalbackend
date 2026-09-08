package com.example.CommerzaGlobalBackend.network.dto;

import com.example.CommerzaGlobalBackend.network.entity.NodeType;

public record NetworkNodeSummaryResponse(Long id, String code, String name, NodeType type, boolean active) {
}
