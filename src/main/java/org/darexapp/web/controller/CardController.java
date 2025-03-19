package org.darexapp.web.controller;

import org.darexapp.card.model.Card;
import org.darexapp.card.model.CardType;
import org.darexapp.card.service.CardService;
import org.darexapp.security.CustomUserDetails;
import org.darexapp.user.model.User;
import org.darexapp.user.service.UserService;
import org.darexapp.wallet.model.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/cards")
public class CardController {

    private final CardService cardService;
    private final UserService userService;


    @Autowired
    public CardController(CardService cardService, UserService userService) {
        this.cardService = cardService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView showCards(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        User currentUser = userService.findById(customUserDetails.getUserId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("currentUser", currentUser);
        modelAndView.setViewName("cards");
        List<Card> cards = cardService.getCardsByUser(currentUser);
        modelAndView.addObject("cards", cards);


        return modelAndView;
    }

    @PostMapping("/physical")
    public String createPhysicalCard(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        User currentUser = userService.findById(customUserDetails.getUserId());

        Wallet defaultWallet = currentUser.getWallets().iterator().next();

        cardService.createPhysicalCard(currentUser, defaultWallet.getId());

        return "redirect:/cards";
    }

}
