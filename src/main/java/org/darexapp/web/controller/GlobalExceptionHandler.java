package org.darexapp.web.controller;

import org.darexapp.exception.InsufficientFundsException;
import org.darexapp.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGeneralException(Exception ex) {
        ModelAndView modelAndView = new ModelAndView("internal-server-error");
        modelAndView.addObject("message", ex.getMessage());
        return modelAndView;
    }

    // Обработка на not found изключения – показване на 404 страница
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNotFoundException(ResourceNotFoundException ex) {
        ModelAndView modelAndView = new ModelAndView("not-found");
        modelAndView.addObject("message", ex.getMessage());
        return modelAndView;
    }

    @ExceptionHandler(InsufficientFundsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleInsufficientFundsException(InsufficientFundsException ex) {
        ModelAndView modelAndView = new ModelAndView("internal-server-error");
        modelAndView.addObject("message", ex.getMessage());
        return modelAndView;
    }
}
