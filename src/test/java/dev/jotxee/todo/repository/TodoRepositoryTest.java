package dev.jotxee.todo.repository;

import dev.jotxee.todo.entities.TodoItemEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//@DataJpaTest
//class TodoRepositoryTest {
//
//    @Autowired
//    private TodoRepository todoRepository;
//
//    @Test
//    void testFindAllByOrderByDoneAscNameAsc() {
//        // Crear y guardar entidades de prueba
//        TodoItemEntity todo1 = TodoItemEntity.builder()
//                .name("Pan Bimbo")
//                .done(false)
//                .build();
//        todoRepository.save(todo1);
//
//        TodoItemEntity todo2 = TodoItemEntity.builder()
//                .name("Arroz basmati")
//                .done(true)
//                .build();
//        todoRepository.save(todo2);
//
//        TodoItemEntity todo3 = TodoItemEntity.builder()
//                .name("Agua")
//                .done(false)
//                .build();
//        todoRepository.save(todo3);
//
//        // Ejecutar la consulta
//        List<TodoItemEntity> todos = todoRepository.findAllByOrderByDoneAscNameAsc().stream().toList();
//
//        // Verificar el orden
//        assertEquals(3, todos.size());
//        assertFalse(todos.get(0).isDone());
//        assertEquals("Agua", todos.get(0).getName());
//
//        assertFalse(todos.get(1).isDone());
//        assertEquals("Pan Bimbo", todos.get(1).getName());
//
//        assertTrue(todos.get(2).isDone());
//        assertEquals("Arroz basmati", todos.get(2).getName());
//    }
//}
