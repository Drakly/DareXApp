package org.darexapp.web.controller;

import org.darexapp.security.CustomUserDetails;
import org.darexapp.transaction.model.Transaction;
import org.darexapp.transaction.service.TransactionService;
import org.darexapp.user.model.User;
import org.darexapp.user.service.UserService;
import org.darexapp.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class WalletController {

    private final UserService userService;// Предполага се, че имаш UserService
    private final WalletService walletService;
    private final TransactionService transactionService;

    @Autowired
    public WalletController(UserService userService, WalletService walletService, TransactionService transactionService) {
        this.userService = userService;
        this.walletService = walletService;
        this.transactionService = transactionService;
    }

    @GetMapping("/wallet")
    public ModelAndView getWallets(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        User user = userService.findById(customUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("wallet");
        modelAndView.addObject("user", user);

        List<Transaction> transactions = transactionService.getAllTransactionsByOwnerId(user.getId());
        modelAndView.addObject("transactions", transactions);

        List<String> categories = List.of("Bills", "Education", "Utility", "Shopping");
        modelAndView.addObject("categories", categories);

        // Филтри (пример)
        List<String> filters = List.of("Group By", "Realisation", "Dates", "Types", "Sample", "Extended");
        modelAndView.addObject("filters", filters);




        return modelAndView; // Шаблонът wallet.html
    }
}
