package ru.ssau.virtualservers.dto;

public record CreateServerRequestDTO(
    String name,
    Long userId,
    int cpu,
    int ram
) {}