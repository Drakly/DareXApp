package org.darexapp.exception;

public class UsernameExistException extends RuntimeException {

    public UsernameExistException(String message) {
        super(message);
    }
}
