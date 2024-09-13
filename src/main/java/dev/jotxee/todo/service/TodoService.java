package dev.jotxee.todo.service;

import dev.jotxee.todo.dto.Todo;
import dev.jotxee.todo.entities.TodoItemEntity;
import dev.jotxee.todo.exception.EntityAlreadyExistsException;
import dev.jotxee.todo.exception.EntityWithOutChangesException;
import dev.jotxee.todo.mapper.TodoMapper;
import dev.jotxee.todo.repository.TodoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.lang.String.format;

@Slf4j
@Service
public class TodoService {

    public static final String ITEM_NOT_FOUND_MSG = "El item con el id %s no existe.";
    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public void createTodo(Todo todo) {
        log.info("Creating: {}", todo);
        // Verificar si ya existe un item con el mismo nombre
        todoRepository.findByNameIgnoreCase(todo.name()).ifPresent(_ -> {
            throw new EntityAlreadyExistsException("El item con el nombre '" + todo.name() + "' ya existe.");
        });

        log.info("Item no existe, creando nuevo item");
        // Si no existe, crear y guardar el nuevo item
        todoRepository.save(TodoItemEntity.builder().name(todo.name()).build());
    }

    public List<Todo> getTodos() {
        return todoRepository.findAllByOrderByDoneAscNameAsc().stream()
                .map(TodoMapper::toDto)
                .toList();
    }

    public Todo updateTodo(Long id, Todo todo) {
        log.info("Updating todo with id: {}", todo);
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
            throw new EntityWithOutChangesException(format("El item con el id %s no ha sido actualizado.", id));
        }
        return TodoMapper.toDto(todoRepository.save(entity));
    }

    public void deleteTodo(Long id) {
        log.info("Deleting TODO with id: {}", id);
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
        log.info("Toggling done for todo with id: {}", id);
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