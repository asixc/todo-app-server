package dev.jotxee.todo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_MODIFIED)
public class EntityWithOutChangesException extends RuntimeException {
    public EntityWithOutChangesException(String message) {
        super(message);
    }
}
