package ru.ssau.virtualservers.dto;

import ru.ssau.virtualservers.entity.enums.UserRole;

public record UserResponseDTO(
    Long id,
    String username,
    UserRole role
) {}
