package org.darexapp.exception;


public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}


class BusinessRuleViolationException extends DomainException {
    
    public BusinessRuleViolationException(String message) {
        super(message);
    }
}


class InvalidStateException extends DomainException {
    
    public InvalidStateException(String message) {
        super(message);
    }
}


class ValidationException extends DomainException {
    
    public ValidationException(String message) {
        super(message);
    }
}


class SecurityViolationException extends DomainException {
    
    public SecurityViolationException(String message) {
        super(message);
    }
}
