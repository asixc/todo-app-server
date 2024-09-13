package dev.jotxee.todo.controller;

import dev.jotxee.todo.dto.Todo;
import dev.jotxee.todo.service.TodoService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("api/v1/todo")
public class TodoController {

    private final TodoService todoService;
    private final TodoWebSocketHandler webSocketHandler;

    public TodoController(TodoService todoService, TodoWebSocketHandler webSocketHandler) {
        this.todoService = todoService;
        this.webSocketHandler = webSocketHandler;
    }

    @PostMapping
    public void createTodo(@RequestBody Todo todo) throws IOException {
        todoService.createTodo(todo);
        webSocketHandler.broadcastMessage("Nuevo Todo creado: " + todo.name());
    }

    @GetMapping
    public List<Todo> getTodos() {
        return todoService.getTodos();
    }

    @PutMapping("/{id}")
    public Todo updateTodo(@PathVariable Long id, @RequestBody Todo todo) throws IOException {
        var updatedTodo = todoService.updateTodo(id, todo);
        webSocketHandler.broadcastMessage("Todo actualizado: " + todo.name());
        return updatedTodo;
    }

    @DeleteMapping("/{id}")
    public void deleteTodo(@PathVariable Long id) throws IOException {
        todoService.deleteTodo(id);
        webSocketHandler.broadcastMessage("Todo eliminado con ID: " + id);
    }

    @GetMapping("/find")
    public List<Todo> findTodos(@RequestParam String name) {
        return todoService.findTodosBy(name);
    }

    @PostMapping("/{id}/mark-done")
    public Todo markTodoDone(@PathVariable Long id) throws IOException {
        todoService.markTodoAsDone(id);  // Marcar como hecho
        webSocketHandler.broadcastMessage("Todo marcado como hecho: " + id);  // Notificar por WebSocket
        return todoService.findById(id);
    }

    // Endpoint para marcar un Todo como no hecho
    @PostMapping("/{id}/mark-undone")
    public Todo markTodoUndone(@PathVariable Long id) throws IOException {
        todoService.markTodoAsUndone(id);  // Marcar como no hecho
        webSocketHandler.broadcastMessage("Todo marcado como no hecho: " + id);  // Notificar por WebSocket
        return todoService.findById(id);
    }
}
