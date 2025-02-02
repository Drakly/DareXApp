package org.darexapp.web.controller;

import jakarta.servlet.http.HttpSession;
import org.darexapp.user.model.User;
import org.darexapp.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;


@Controller
@RequestMapping("/users")
public class UserController {


    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}/profile")
    public ModelAndView getUserProfile(HttpSession session) {
        UUID userId = (UUID) session.getAttribute("user_id");
        User user = userService.findById(userId);

        ModelAndView mav = new ModelAndView();
        mav.addObject("user", user);
        mav.setViewName("profile");
        return mav;

    }
}
