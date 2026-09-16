package dev.jotxee.todo.controller;

import dev.jotxee.todo.dto.Todo;
import dev.jotxee.todo.dto.QuantityUpdateRequest;
import dev.jotxee.todo.service.TodoService;
import dev.jotxee.todo.util.LogMask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/todo")
public class TodoController {

    private static final Logger log = LoggerFactory.getLogger(TodoController.class);

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createTodo(@Valid @RequestBody Todo todo) {
        todoService.createTodo(todo);
    }

    @GetMapping
    public List<Todo> getTodos() {
        return todoService.getTodos();
    }

    @PutMapping("/{id}")
    public Todo updateTodo(@PathVariable Long id, @Valid @RequestBody Todo todo) {
        return todoService.updateTodo(id, todo);
    }

    @PatchMapping("/{id}/quantity")
    public Todo updateTodoQuantity(@PathVariable Long id,
                                   @Valid @RequestBody QuantityUpdateRequest request) {
        return todoService.updateQuantity(id, request.quantity());
    }

    @DeleteMapping("/{id}")
    public void deleteTodo(@PathVariable Long id,
                           @AuthenticationPrincipal String email) {
        log.atInfo()
                .addArgument(() -> LogMask.partial(email))
                .addArgument(id)
                .log("Delete requested by={} for todo id={}");
        todoService.deleteTodo(id);
    }

    @GetMapping("/find")
    public List<Todo> findTodos(@RequestParam String name) {
        return todoService.findTodosBy(name);
    }

    @PostMapping("/{id}/mark-done")
    public void markTodoDone(@PathVariable Long id) {
        todoService.markTodoAsDone(id);
    }

    @PostMapping("/{id}/mark-undone")
    public void markTodoUndone(@PathVariable Long id) {
        todoService.markTodoAsUndone(id);
    }
}
