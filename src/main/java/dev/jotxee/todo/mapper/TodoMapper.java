package dev.jotxee.todo.mapper;

import dev.jotxee.todo.dto.Todo;
import dev.jotxee.todo.entities.TodoItemEntity;

public class TodoMapper {
    public static TodoItemEntity toEntity(Todo todo) {
        return TodoItemEntity.builder()
                .id(todo.id())
                .name(todo.name())
                .done(todo.done())
                .quantity(todo.quantity())
                .build();
    }

    public static Todo toDto(TodoItemEntity entity) {
        return new Todo(entity.getId(), entity.getName(), entity.isDone(), entity.getQuantity());
    }
}
