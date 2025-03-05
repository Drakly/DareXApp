package org.darexapp.exception;


public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}


class ResourceNotFoundException extends DomainException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceType, String identifier) {
        super(String.format("%s with identifier [%s] not found", resourceType, identifier));
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


class InsufficientFundsException extends DomainException {
    
    public InsufficientFundsException(String message) {
        super(message);
    }
}


class SecurityViolationException extends DomainException {
    
    public SecurityViolationException(String message) {
        super(message);
    }
}
