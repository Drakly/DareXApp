//package org.darexapp.exception;
//
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.validation.FieldError;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.servlet.ModelAndView;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//
//
//@ControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(ResourceNotFoundException.class)
//    public ModelAndView handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
//        return createErrorModelAndView(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
//    }
//
//    @ExceptionHandler(BusinessRuleViolationException.class)
//    public ModelAndView handleBusinessRuleViolationException(BusinessRuleViolationException ex, HttpServletRequest request) {
//        return createErrorModelAndView(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
//    }
//
//    @ExceptionHandler(InvalidStateException.class)
//    public ModelAndView handleInvalidStateException(InvalidStateException ex, HttpServletRequest request) {
//        return createErrorModelAndView(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
//    }
//
//    @ExceptionHandler(ValidationException.class)
//    public ModelAndView handleValidationException(ValidationException ex, HttpServletRequest request) {
//        return createErrorModelAndView(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
//    }
//
//    @ExceptionHandler(InsufficientFundsException.class)
//    public ModelAndView handleInsufficientFundsException(InsufficientFundsException ex, HttpServletRequest request) {
//        return createErrorModelAndView(HttpStatus.PAYMENT_REQUIRED, ex.getMessage(), request.getRequestURI());
//    }
//
//    @ExceptionHandler(SecurityViolationException.class)
//    public ModelAndView handleSecurityViolationException(SecurityViolationException ex, HttpServletRequest request) {
//        return createErrorModelAndView(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI());
//    }
//
//    @ExceptionHandler(AccessDeniedException.class)
//    public ModelAndView handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
//        return createErrorModelAndView(HttpStatus.FORBIDDEN, "Access denied", request.getRequestURI());
//    }
//
//    @ExceptionHandler(BadCredentialsException.class)
//    public ModelAndView handleBadCredentialsException(BadCredentialsException ex, HttpServletRequest request) {
//        return createErrorModelAndView(HttpStatus.UNAUTHORIZED, "Invalid credentials", request.getRequestURI());
//    }
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ModelAndView handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getAllErrors().forEach(error -> {
//            String fieldName = ((FieldError) error).getField();
//            String errorMessage = error.getDefaultMessage();
//            errors.put(fieldName, errorMessage);
//        });
//
//        ModelAndView mav = new ModelAndView();
//        mav.addObject("status", HttpStatus.BAD_REQUEST.value());
//        mav.addObject("message", "Validation failed");
//        mav.addObject("timestamp", LocalDateTime.now());
//        mav.addObject("path", request.getRequestURI());
//        mav.addObject("errors", errors);
//        mav.setViewName("error");
//        return mav;
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ModelAndView handleAllUncaughtException(Exception ex, HttpServletRequest request) {
//        log.error("Unexpected error occurred", ex);
//        return createErrorModelAndView(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request.getRequestURI());
//    }
//
//    private ModelAndView createErrorModelAndView(HttpStatus status, String message, String path) {
//        ModelAndView mav = new ModelAndView();
//        mav.addObject("status", status.value());
//        mav.addObject("message", message);
//        mav.addObject("timestamp", LocalDateTime.now());
//        mav.addObject("path", path);
//        mav.setViewName("error");
//        return mav;
//    }
//}
