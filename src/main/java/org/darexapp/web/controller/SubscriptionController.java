package org.darexapp.web.controller;

import org.darexapp.security.CustomUserDetails;
import org.darexapp.subscription.model.SubscriptionType;
import org.darexapp.subscription.service.SubscriptionService;
import org.darexapp.transaction.model.Transaction;
import org.darexapp.user.model.User;
import org.darexapp.user.service.UserService;
import org.darexapp.web.dto.UpgradeSubscriptionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final UserService userService;
    private final SubscriptionService subscriptionService;

    @Autowired
    public SubscriptionController(UserService userService, SubscriptionService subscriptionService) {
        this.userService = userService;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping
    public ModelAndView showUpgradeOptions(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        User currentUser = userService.findById(customUserDetails.getUserId());
        ModelAndView mav = new ModelAndView("subscriptions");
        mav.addObject("currentUser", currentUser);
        mav.addObject("upgradeRequest", UpgradeSubscriptionRequest.builder().build());
        return mav;
    }

    @PostMapping
    public String processUpgrade(@RequestParam("subscription-type") SubscriptionType subscriptionType,
                                 UpgradeSubscriptionRequest upgradeRequest,
                                 @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        User currentUser = userService.findById(customUserDetails.getUserId());
        Transaction transactionResult = subscriptionService.upgradeSubscription(currentUser, subscriptionType, upgradeRequest);
        return "redirect:/transactions/" + transactionResult.getId();
    }

}
