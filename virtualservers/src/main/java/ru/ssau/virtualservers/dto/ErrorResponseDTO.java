package ru.ssau.virtualservers.dto;

import java.time.LocalDateTime;

public record ErrorResponseDTO(
    int status,
    String message,
    LocalDateTime timestamp
) {}