package org.darexapp.web.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.darexapp.security.CustomUserDetails;
import org.darexapp.transaction.model.Transaction;
import org.darexapp.transaction.service.TransactionService;
import org.darexapp.user.model.User;
import org.darexapp.user.service.UserService;
import org.darexapp.web.dto.LoginRequest;
import org.darexapp.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;


import java.util.List;
import java.util.UUID;

@Controller
public class IndexController {

    private final UserService userService;

    @Autowired
    public IndexController(UserService userService, TransactionService transactionService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public ModelAndView showIndexPage() {
        return new ModelAndView("index");
    }

    @GetMapping("/login")
    public ModelAndView showLoginPage(@RequestParam(value = "error", required = false) String error) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("login");
        mav.addObject("loginRequest", new LoginRequest());

        if (error != null) {
            mav.addObject("errorMessage", "Invalid username or password.");
        }

        return mav;
    }


    @GetMapping("/register")
    public ModelAndView showRegisterPage() {
        ModelAndView mav = new ModelAndView("register");
        mav.addObject("registerRequest", new RegisterRequest());
        return mav;
    }

    @PostMapping("/register")
    public ModelAndView handleRegister(@Valid RegisterRequest registerRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("register");
        }

        userService.register(registerRequest);

        return new ModelAndView("redirect:/login");
    }

    @GetMapping("/home")
    public ModelAndView showHomePage(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        User user = userService.findById(customUserDetails.getUserId());

        ModelAndView mav = new ModelAndView();
        mav.setViewName("home");
        mav.addObject("user", user);

        return mav;
    }

}
