package org.darexapp.web.controller;

import org.darexapp.security.CustomUserDetails;
import org.darexapp.user.model.User;
import org.darexapp.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    private final UserService userService;

    @Autowired
    public SettingsController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping
    public ModelAndView showSettings(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        User currentUser = userService.findById(customUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("settings");
        modelAndView.addObject("currentUser", currentUser);

        return modelAndView;
    }
}
