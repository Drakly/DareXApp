package org.darexapp.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GlobalExceptionHandler extends RuntimeException {

    private final String message;
    private final HttpStatus status;


    public GlobalExceptionHandler(String message, HttpStatus status) {
        super(message);
        this.message = message;
        this.status = status;
    }
}
