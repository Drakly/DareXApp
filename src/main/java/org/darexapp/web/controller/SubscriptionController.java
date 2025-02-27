package org.darexapp.web.controller;

import org.darexapp.security.CustomUserDetails;
import org.darexapp.subscription.model.Subscription;
import org.darexapp.subscription.model.SubscriptionStatus;
import org.darexapp.subscription.model.SubscriptionType;
import org.darexapp.subscription.repository.SubscriptionRepository;
import org.darexapp.subscription.service.SubscriptionService;
import org.darexapp.transaction.model.Transaction;
import org.darexapp.user.model.User;
import org.darexapp.user.service.UserService;
import org.darexapp.wallet.model.Wallet;
import org.darexapp.wallet.service.WalletService;
import org.darexapp.web.dto.UpgradeSubscriptionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;
import java.util.UUID;

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

    @GetMapping("/upgrade")
    public ModelAndView showUpgradeOptions(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        User currentUser = userService.findById(customUserDetails.getUserId());
        ModelAndView mav = new ModelAndView("subscriptions");
        mav.addObject("currentUser", currentUser);
        mav.addObject("upgradeRequest", UpgradeSubscriptionRequest.builder().build());
        return mav;
    }

    @PostMapping("/upgrade")
    public String processUpgrade(@RequestParam("subscription-type") SubscriptionType subType,
                                 UpgradeSubscriptionRequest upgradeRequest,
                                 @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        User currentUser = userService.findById(customUserDetails.getUserId());
        Transaction transactionResult = subscriptionService.upgradeSubscription(currentUser, subType, upgradeRequest);
        return "redirect:/transactions/" + transactionResult.getId();
    }

    @GetMapping("/history")
    public ModelAndView viewSubscriptionHistory(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        User currentUser = userService.findById(customUserDetails.getUserId());
        ModelAndView mav = new ModelAndView("subscription-history");
        mav.addObject("currentUser", currentUser);
        return mav;
    }
}
