package dev.jotxee.todo.dto;

import jakarta.validation.Valid;

public record QuantityUpdateRequest(@Valid Quantity quantity) {
}
