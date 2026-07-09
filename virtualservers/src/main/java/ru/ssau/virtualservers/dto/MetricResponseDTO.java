package ru.ssau.virtualservers.dto;

import java.time.LocalDateTime;

public record MetricResponseDTO(
    Integer cpuUsage,
    Integer ramUsage,
    Integer networkLoad,
    LocalDateTime timestamp
) {}
