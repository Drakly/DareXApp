package org.darexapp.web.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.darexapp.user.model.User;
import org.darexapp.user.service.UserService;
import org.darexapp.web.dto.EditUserRequest;
import org.darexapp.web.mapper.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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
    public ModelAndView getUserProfile(@PathVariable UUID id) {
        User user = userService.findById(id);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("profile-menu");
        modelAndView.addObject("user", user);
        modelAndView.addObject("userEditRequest", DtoMapper.editUserMapper(user));

        return modelAndView;

    }

    @PutMapping("/{id}/profile")
    public ModelAndView updateUserProfile(@PathVariable UUID id, @Valid @ModelAttribute EditUserRequest userEditRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            User user = userService.findById(id);
            ModelAndView modelAndView = new ModelAndView("profile-menu");
            modelAndView.addObject("user", user);
            modelAndView.addObject("userEditRequest", userEditRequest);
            return modelAndView;
        }

        userService.updateUser(id, userEditRequest);

        return new ModelAndView("redirect:/home");
    }
}
