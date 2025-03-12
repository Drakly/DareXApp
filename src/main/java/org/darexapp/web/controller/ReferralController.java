package org.darexapp.web.controller;

import org.darexapp.referral.client.dto.ReferralRequest;
import org.darexapp.referral.service.ReferralService;
import org.darexapp.security.CustomUserDetails;
import org.darexapp.user.model.User;
import org.darexapp.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ModelAndView showReferralPage(@AuthenticationPrincipal CustomUserDetails userDetails, ReferralRequest referralRequest) {
        User userId = userService.findById(userDetails.getUserId());
        ReferralRequest re = referralService.getReferral(userId.getId());

        ModelAndView mav = new ModelAndView();
        mav.setViewName("referral");
        mav.addObject("user", userDetails);
        mav.addObject("referralRequest", new ReferralRequest());
        return mav;
    }

    @PostMapping("/create")
    public ModelAndView createReferral(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User userId = userService.findById(userDetails.getUserId());
        ReferralRequest referralRequest = referralService.createReferral(userId.getId());

        ModelAndView mav = new ModelAndView();
        mav.setViewName("redirect:/referrals/" + referralRequest.getId());
        mav.addObject("referral", referralRequest);
        mav.addObject("user", userId);
        return mav;
    }

    @GetMapping("/{userId}")
    public ModelAndView getReferral(@PathVariable UUID userId) {
        ReferralRequest referral = referralService.getReferral(userId);
        ModelAndView mav = new ModelAndView("referral");
        mav.addObject("referral", referral);
        return mav;
    }

    @PostMapping("/track/{referralCode}")
    public ModelAndView trackReferral(@PathVariable String referralCode) {
        referralService.incrementClickCount(referralCode);
        ModelAndView mav = new ModelAndView("referral");
        mav.addObject("message", "Referral click tracked successfully");
        return mav;
    }
}
