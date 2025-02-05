package org.darexapp.web.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.darexapp.user.model.User;
import org.darexapp.user.service.UserService;
import org.darexapp.web.dto.LoginRequest;
import org.darexapp.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;


import java.util.UUID;

@Controller
public class IndexController {

    private final UserService userService;

    @Autowired
    public IndexController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public ModelAndView showIndexPage() {
        return new ModelAndView("index");
    }

    @GetMapping("/login")
    public ModelAndView showLoginPage() {
        ModelAndView mav = new ModelAndView("login");
        mav.addObject("loginRequest", new LoginRequest());
        return mav;
    }

    @PostMapping("/login")
    public ModelAndView handleLogin(@ModelAttribute("loginRequest") @Valid LoginRequest loginRequest,
                                    BindingResult bindingResult, HttpSession session) {
        ModelAndView mav = new ModelAndView("login");

        if (bindingResult.hasErrors()) {
            mav.addObject("loginRequest", loginRequest);
            return mav;
        }

        User loggedUser = userService.login(loginRequest);
        session.setAttribute("loggedUser", loggedUser);
        mav.setViewName("redirect:/home");

        return mav;
    }

    @GetMapping("/register")
    public ModelAndView showRegisterPage() {
        ModelAndView mav = new ModelAndView("register");
        mav.addObject("registerDTO", new RegisterRequest());
        return mav;
    }

    @PostMapping("/register")
    public ModelAndView handleRegister(@ModelAttribute("registerDTO") @Valid RegisterRequest registerDTO, 
                                     BindingResult bindingResult) {
        ModelAndView mav = new ModelAndView();
        
        if (bindingResult.hasErrors()) {
            mav.setViewName("register");
            mav.addObject("registerDTO", registerDTO);
            return mav;
        }

        userService.register(registerDTO);
        mav.setViewName("redirect:/login");
        mav.addObject("registrationSuccess", true);
        
        return mav;
    }

    @GetMapping("/home")
    public ModelAndView showHomePage(HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
        
        ModelAndView mav = new ModelAndView("home");
        mav.addObject("user", user);
        return mav;
    }

    @GetMapping("/logout")
    public String getLogoutPage(HttpSession session) {

        session.invalidate();
        return "redirect:/";
    }
}
