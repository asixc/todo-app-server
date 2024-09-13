package dev.jotxee.todo.dto;

public record Todo(
        Long id,
        String name,
        boolean done,
        Long quantity
) {
}
