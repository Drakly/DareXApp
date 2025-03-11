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

    @GetMapping()
    public ModelAndView showCreateForm(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        User currentUser = userService.findById(customUserDetails.getUserId());
        ReferralRequest referralRequest = new ReferralRequest();
        referralRequest.setUserId(currentUser.getId());
        ModelAndView mav = new ModelAndView("referral");
        mav.addObject("referral", referralRequest);
        return mav;
    }

    @PostMapping("/create")
    public ModelAndView createReferral(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                       @ModelAttribute("referral") ReferralRequest referralRequest) {
        referralRequest.setUserId(customUserDetails.getUserId());

        referralService.createReferral(
                referralRequest.getUserId(),
                referralRequest.getReferralCode(),
                referralRequest.getCreatedAt(),
                referralRequest.getClickCount()
        );

        ModelAndView mav = new ModelAndView("referral");
        mav.addObject("message", "Referral created successfully");
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
