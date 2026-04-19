package dev.jotxee.todo.entities;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "items")
public class TodoItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private boolean done;
    private Long quantity;

    public TodoItemEntity() {}

    public TodoItemEntity(Long id, String name, boolean done, Long quantity) {
        this.id = id;
        this.name = name;
        this.done = done;
        this.quantity = quantity;
    }

    public static TodoItemEntity withName(String name) {
        TodoItemEntity entity = new TodoItemEntity();
        entity.name = name;
        return entity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }

    public Long getQuantity() { return quantity; }
    public void setQuantity(Long quantity) { this.quantity = quantity; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TodoItemEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "TodoItemEntity{id=" + id + ", name='" + name + "', done=" + done + ", quantity=" + quantity + "}";
    }
}
