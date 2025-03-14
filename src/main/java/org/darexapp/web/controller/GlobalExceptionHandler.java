package org.darexapp.web.controller;

import org.darexapp.exception.EmailAddressAlreadyExist;
import org.darexapp.exception.UsernameExistException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGeneralException(Exception ex) {
        ModelAndView modelAndView = new ModelAndView("internal-server-error");
        modelAndView.addObject("message", ex.getClass().getSimpleName());
        return modelAndView;
    }

    @ExceptionHandler(EmailAddressAlreadyExist.class)
    public String handleEmailAddressAlreadyExist(EmailAddressAlreadyExist ex, RedirectAttributes redirectAttributes) {

        String message = ex.getMessage();

        redirectAttributes.addFlashAttribute("emailExistException", message);
        return "redirect:/register";
    }

    @ExceptionHandler(UsernameExistException.class)
    public String handleUsernameAddressAlreadyExist(UsernameExistException ex, RedirectAttributes redirectAttributes) {

        String message = ex.getMessage();

        redirectAttributes.addFlashAttribute("usernameExistException", message);
        return "redirect:/register";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNotFoundException(Exception ex) {
        ModelAndView modelAndView = new ModelAndView("not-found");
        modelAndView.addObject("message", ex.getClass().getSimpleName());
        return modelAndView;
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleInsufficientFundsException(Exception ex) {
        ModelAndView modelAndView = new ModelAndView("internal-server-error");
        modelAndView.addObject("message", ex.getClass().getSimpleName());
        return modelAndView;
    }
}
