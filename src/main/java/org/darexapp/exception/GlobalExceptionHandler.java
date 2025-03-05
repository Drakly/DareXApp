//package org.darexapp.exception;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.validation.BindException;
//import org.springframework.validation.FieldError;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.context.request.WebRequest;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//
//@Slf4j
//@ControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(ResourceNotFoundException.class)
//    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
//            ResourceNotFoundException ex,
//            WebRequest request
//    ) {
//        return createErrorResponse(
//                HttpStatus.NOT_FOUND,
//                ex.getMessage(),
//                request.getDescription(false)
//        );
//    }
//
//    @ExceptionHandler(BusinessRuleViolationException.class)
//    public ResponseEntity<ErrorResponse> handleBusinessRuleViolationException(
//            BusinessRuleViolationException ex,
//            WebRequest request
//    ) {
//        return createErrorResponse(
//                HttpStatus.CONFLICT,
//                ex.getMessage(),
//                request.getDescription(false)
//        );
//    }
//
//    @ExceptionHandler(InvalidStateException.class)
//    public ResponseEntity<ErrorResponse> handleInvalidStateException(
//            InvalidStateException ex,
//            WebRequest request
//    ) {
//        return createErrorResponse(
//                HttpStatus.CONFLICT,
//                ex.getMessage(),
//                request.getDescription(false)
//        );
//    }
//
//    @ExceptionHandler(ValidationException.class)
//    public ResponseEntity<ErrorResponse> handleValidationException(
//            ValidationException ex,
//            WebRequest request
//    ) {
//        return createErrorResponse(
//                HttpStatus.BAD_REQUEST,
//                ex.getMessage(),
//                request.getDescription(false)
//        );
//    }
//
//    @ExceptionHandler(InsufficientFundsException.class)
//    public ResponseEntity<ErrorResponse> handleInsufficientFundsException(
//            InsufficientFundsException ex,
//            WebRequest request
//    ) {
//        return createErrorResponse(
//                HttpStatus.PAYMENT_REQUIRED,
//                ex.getMessage(),
//                request.getDescription(false)
//        );
//    }
//
//    @ExceptionHandler(SecurityViolationException.class)
//    public ResponseEntity<ErrorResponse> handleSecurityViolationException(
//            SecurityViolationException ex,
//            WebRequest request
//    ) {
//        return createErrorResponse(
//                HttpStatus.FORBIDDEN,
//                ex.getMessage(),
//                request.getDescription(false)
//        );
//    }
//
//    @ExceptionHandler(AccessDeniedException.class)
//    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
//            AccessDeniedException ex,
//            WebRequest request
//    ) {
//        return createErrorResponse(
//                HttpStatus.FORBIDDEN,
//                "Access denied",
//                request.getDescription(false)
//        );
//    }
//
//    @ExceptionHandler(BadCredentialsException.class)
//    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
//            BadCredentialsException ex,
//            WebRequest request
//    ) {
//        return createErrorResponse(
//                HttpStatus.UNAUTHORIZED,
//                "Invalid credentials",
//                request.getDescription(false)
//        );
//    }
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
//            MethodArgumentNotValidException ex
//    ) {
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getAllErrors().forEach(error -> {
//            String fieldName = ((FieldError) error).getField();
//            String errorMessage = error.getDefaultMessage();
//            errors.put(fieldName, errorMessage);
//        });
//
//        ValidationErrorResponse response = new ValidationErrorResponse(
//                HttpStatus.BAD_REQUEST.value(),
//                "Validation failed",
//                LocalDateTime.now(),
//                errors
//        );
//
//        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleAllUncaughtException(
//            Exception ex,
//            WebRequest request
//    ) {
//        log.error("Unexpected error occurred", ex);
//        return createErrorResponse(
//                HttpStatus.INTERNAL_SERVER_ERROR,
//                "An unexpected error occurred",
//                request.getDescription(false)
//        );
//    }
//
//    private ResponseEntity<ErrorResponse> createErrorResponse(
//            HttpStatus status,
//            String message,
//            String path
//    ) {
//        ErrorResponse errorResponse = new ErrorResponse(
//                status.value(),
//                message,
//                LocalDateTime.now(),
//                path
//        );
//        return new ResponseEntity<>(errorResponse, status);
//    }
//}
//
//record ErrorResponse(
//        int status,
//        String message,
//        LocalDateTime timestamp,
//        String path
//) {}
//
//record ValidationErrorResponse(
//        int status,
//        String message,
//        LocalDateTime timestamp,
//        Map<String, String> errors
//) {}