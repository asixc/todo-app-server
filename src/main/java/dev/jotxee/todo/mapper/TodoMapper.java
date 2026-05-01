package dev.jotxee.todo.mapper;

import dev.jotxee.todo.dto.Todo;
import dev.jotxee.todo.entities.TodoItemEntity;

public class TodoMapper {
    private TodoMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static TodoItemEntity toEntity(Todo todo) {
        return new TodoItemEntity(todo.id(), todo.name(), todo.done(), todo.quantity());
    }

    public static Todo toDto(TodoItemEntity entity) {
        return new Todo(entity.getId(), entity.getName(), entity.isDone(), entity.getQuantity());
    }
}
