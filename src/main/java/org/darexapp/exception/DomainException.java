package org.darexapp.exception;

/**
 * Base exception class for all domain-specific exceptions in the application.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Exception thrown when a requested resource is not found.
 */
class ResourceNotFoundException extends DomainException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceType, String identifier) {
        super(String.format("%s with identifier [%s] not found", resourceType, identifier));
    }
}

/**
 * Exception thrown when a business rule is violated.
 */
class BusinessRuleViolationException extends DomainException {
    
    public BusinessRuleViolationException(String message) {
        super(message);
    }
}

/**
 * Exception thrown when an operation is not allowed due to the current state.
 */
class InvalidStateException extends DomainException {
    
    public InvalidStateException(String message) {
        super(message);
    }
}

/**
 * Exception thrown when validation fails.
 */
class ValidationException extends DomainException {
    
    public ValidationException(String message) {
        super(message);
    }
}

/**
 * Exception thrown when there are insufficient funds for an operation.
 */
class InsufficientFundsException extends DomainException {
    
    public InsufficientFundsException(String message) {
        super(message);
    }
}

/**
 * Exception thrown when there are security-related issues.
 */
class SecurityViolationException extends DomainException {
    
    public SecurityViolationException(String message) {
        super(message);
    }
}
