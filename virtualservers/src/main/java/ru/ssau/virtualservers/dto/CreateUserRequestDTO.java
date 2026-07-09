package ru.ssau.virtualservers.dto;

public record CreateUserRequestDTO(
    String username,
    String password
) {}
