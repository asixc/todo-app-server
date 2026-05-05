package dev.jotxee.todo.dto;

public record Todo(
        Long id,
        String name,
        Boolean done,
        Long quantity
) {
}
