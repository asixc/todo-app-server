package dev.jotxee.todo.service;

import dev.jotxee.todo.dto.Quantity;
import dev.jotxee.todo.entities.TodoItemEntity;
import dev.jotxee.todo.repository.TodoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private TodoService todoService;

    @Test
    void updatesQuantityWithDecimalValueAndUnit() {
        TodoItemEntity entity = new TodoItemEntity(
                1L,
                "Leche",
                false,
                null
        );
        when(todoRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(todoRepository.save(any(TodoItemEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = todoService.updateQuantity(1L, new Quantity(new BigDecimal("0.4"), "kg"));

        assertThat(result.quantity()).isEqualTo(new Quantity(new BigDecimal("0.4"), "kg"));
        verify(todoRepository).save(entity);
    }

    @Test
    void clearsQuantityWhenRequestIsNull() {
        TodoItemEntity entity = new TodoItemEntity(
                1L,
                "Leche",
                false,
                new Quantity(new BigDecimal("400"), "g")
        );
        when(todoRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(todoRepository.save(any(TodoItemEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = todoService.updateQuantity(1L, null);

        assertThat(result.quantity()).isNull();
        verify(todoRepository).save(entity);
    }
}
