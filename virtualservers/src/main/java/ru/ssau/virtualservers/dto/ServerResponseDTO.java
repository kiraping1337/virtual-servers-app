package ru.ssau.virtualservers.dto;

import ru.ssau.virtualservers.entity.enums.ServerStatus;

public record ServerResponseDTO(
    Long id,
    String name,
    String ipAddress,
    ServerStatus status,
    Integer cpu,
    Integer ram
) {}
