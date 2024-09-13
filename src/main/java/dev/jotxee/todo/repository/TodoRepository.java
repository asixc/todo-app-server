package dev.jotxee.todo.repository;

import dev.jotxee.todo.entities.TodoItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<TodoItemEntity, Long> {
     Optional<TodoItemEntity> findByNameIgnoreCase(String name);
     Collection<TodoItemEntity> findByNameContainingIgnoreCaseOrderByName(String name);
     Collection<TodoItemEntity> findAllByOrderByDoneAscNameAsc();
}
