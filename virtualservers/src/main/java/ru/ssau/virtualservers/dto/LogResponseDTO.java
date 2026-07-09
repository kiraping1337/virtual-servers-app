package ru.ssau.virtualservers.dto;

import java.time.LocalDateTime;
import ru.ssau.virtualservers.entity.enums.LogLevel;

public record LogResponseDTO(
    Long id,
    LocalDateTime createdAt,
    LogLevel level,
    String message
) {}
