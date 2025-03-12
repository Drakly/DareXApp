package org.darexapp.web.controller;

import jakarta.validation.Valid;
import org.darexapp.referral.client.dto.ReferralRequest;
import org.darexapp.referral.service.ReferralService;
import org.darexapp.security.CustomUserDetails;
import org.darexapp.user.model.User;
import org.darexapp.user.service.UserService;
import org.darexapp.web.mapper.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequestMapping("/referrals")
public class ReferralController {

    private final ReferralService referralService;
    private final UserService userService;


    @Autowired
    public ReferralController(ReferralService referralService, UserService userService) {
        this.referralService = referralService;
        this.userService = userService;
    }


    @GetMapping
    public ModelAndView showReferralPage(@AuthenticationPrincipal CustomUserDetails userDetails) {
        // Вземаме текущия потребител
        User currentUser = userService.findById(userDetails.getUserId());
        // Вземаме referral за потребителя - ако няма, getReferral трябва да върне празен обект (напр. с празен код и clickCount=0)
        ReferralRequest referral = referralService.getReferral(currentUser.getId());
        ModelAndView modelAndView = new ModelAndView("referral");
        modelAndView.addObject("currentUser", currentUser);
        modelAndView.addObject("referral", referral);
        return modelAndView;
    }

    @PostMapping("/create")
    public ModelAndView createReferral(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @Valid ReferralRequest referralRequest) {
        // Задаваме ID-то на текущия потребител в заявката, ако не е подадено
        User currentUser = userService.findById(userDetails.getUserId());
        referralRequest.setUserId(currentUser.getId());
        referralService.createReferral(referralRequest);
        // Пренасочваме към GET /referrals, за да презаредим страницата с обновени данни
        return new ModelAndView("redirect:/referrals");
    }

    @PostMapping("/track/{referralCode}")
    public ModelAndView trackReferral(@PathVariable String referralCode) {
        referralService.incrementClickCount(referralCode);
        // Пренасочваме към страницата след проследяване на клика
        return new ModelAndView("redirect:/referrals");
    }
}
