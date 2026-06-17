package dev.jotxee.todo.service;

import dev.jotxee.todo.dto.Todo;
import dev.jotxee.todo.entities.TodoItemEntity;
import dev.jotxee.todo.exception.EntityAlreadyExistsException;
import dev.jotxee.todo.exception.EntityWithOutChangesException;
import dev.jotxee.todo.mapper.TodoMapper;
import dev.jotxee.todo.repository.TodoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.lang.String.format;

@Service
public class TodoService {

    private static final Logger log = LoggerFactory.getLogger(TodoService.class);

    public static final String ITEM_NOT_FOUND_MSG = "Item with id %s not found.";
    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public void createTodo(Todo todo) {
        log.info("Creating todo: {}", todo);
        todoRepository.findByNameIgnoreCase(todo.name()).ifPresent(_ -> {
            throw new EntityAlreadyExistsException("Item with name '" + todo.name() + "' already exists.");
        });

        log.info("Saving new todo: {}", todo.name());
        todoRepository.save(TodoItemEntity.withName(todo.name().trim()));
    }

    public List<Todo> getTodos() {
        return todoRepository.findAllOrdered().stream()
                .map(TodoMapper::toDto)
                .toList();
    }

    public Todo updateTodo(Long id, Todo todo) {
        log.info("Updating todo id={}: {}", id, todo);
        boolean changed = false;
        TodoItemEntity entity = todoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(format(ITEM_NOT_FOUND_MSG, id)));
        if (Strings.isNotBlank(todo.name()) && !todo.name().equalsIgnoreCase(entity.getName())) {
            changed = true;
            entity.setName(todo.name());
        }
        if (todo.quantity() != null && !todo.quantity().equals(entity.getQuantity())) {
            entity.setQuantity(todo.quantity());
            changed = true;
        }
        if (!changed) {
            throw new EntityWithOutChangesException(format("Item with id %s has no changes to update.", id));
        }
        return TodoMapper.toDto(todoRepository.save(entity));
    }

    public void deleteTodo(Long id) {
        log.info("Deleting todo id={}, element={}", id, todoRepository.findById(id).orElse(null));
        todoRepository.deleteById(id);
    }

    public List<Todo> findTodosBy(String name) {
        return todoRepository.findByNameContainingIgnoreCaseOrderByName(name).stream()
                .map(TodoMapper::toDto)
                .toList();
    }

    public Todo findById(Long id) {
        return todoRepository.findById(id)
                .map(TodoMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException(format(ITEM_NOT_FOUND_MSG, id)));
    }

    public void toggleTodoDone(Long id) {
        log.info("Toggling done state for todo id={}", id);
        TodoItemEntity entity = todoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(format(ITEM_NOT_FOUND_MSG, id)));
        entity.setDone(!entity.isDone());
        todoRepository.save(entity);
    }

    public void markTodoAsUndone(Long id) {
        TodoItemEntity entity = todoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(format(ITEM_NOT_FOUND_MSG, id)));
        entity.setDone(false);
        todoRepository.save(entity);
    }

    public void markTodoAsDone(Long id) {
        TodoItemEntity entity = todoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(format(ITEM_NOT_FOUND_MSG, id)));
        entity.setDone(true);
        todoRepository.save(entity);
    }

}