package dev.jotxee.todo.dto;

import jakarta.validation.Valid;

public record Todo(
        Long id,
        String name,
        Boolean done,
        @Valid Quantity quantity
) {
}
