package org.darexapp.web.controller;

import org.darexapp.security.CustomUserDetails;
import org.darexapp.subscription.model.Subscription;
import org.darexapp.subscription.model.SubscriptionStatus;
import org.darexapp.subscription.repository.SubscriptionRepository;
import org.darexapp.subscription.service.SubscriptionService;
import org.darexapp.user.model.User;
import org.darexapp.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@Controller
public class SubscriptionController {

    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final SubscriptionRepository subscriptionRepository;

    @Autowired
    public SubscriptionController(UserService userService, SubscriptionService subscriptionService, SubscriptionRepository subscriptionRepository) {
        this.userService = userService;
        this.subscriptionService = subscriptionService;
        this.subscriptionRepository = subscriptionRepository;
    }


    @GetMapping("/subscriptions")
    public ModelAndView showSubscirptionPage(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        User user = userService.findById(customUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("subscriptions");
        modelAndView.addObject("user", user);

        Optional<Subscription> activeSubscription = subscriptionRepository.findByStatusAndOwnerId(SubscriptionStatus.ACTIVE, user.getId());
        modelAndView.addObject("subscription", activeSubscription.orElse(null));

        return new ModelAndView();
    }
}
