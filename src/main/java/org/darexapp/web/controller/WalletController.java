package org.darexapp.web.controller;

import org.darexapp.security.CustomUserDetails;
import org.darexapp.transaction.model.Transaction;
import org.darexapp.transaction.service.TransactionService;
import org.darexapp.user.model.User;
import org.darexapp.user.service.UserService;
import org.darexapp.wallet.model.Wallet;
import org.darexapp.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
public class WalletController {

    private final UserService userService;
    private final WalletService walletService;
    private final TransactionService transactionService;

    @Autowired
    public WalletController(UserService userService, WalletService walletService, TransactionService transactionService) {
        this.userService = userService;
        this.walletService = walletService;
        this.transactionService = transactionService;
    }

    @GetMapping("/wallets")
    public ModelAndView getWallets(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        User user = userService.findById(customUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("wallet");
        modelAndView.addObject("user", user);

        List<Transaction> transactions = transactionService.getAllTransactionsByOwnerId(user.getId());
        modelAndView.addObject("recentTransactions", transactions);

        List<Wallet> sortedWallets = walletService.getSortedWalletsByOwnerId(user.getId());
        modelAndView.addObject("sortedWallets", sortedWallets);
        return modelAndView;
    }

    @PostMapping("/wallets/create-investment")
    public String createInvestmentWallet(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        User user = userService.findById(customUserDetails.getUserId());
        walletService.createInvestmentWallet(user);

        return "redirect:/wallets";
    }

    @PutMapping("/wallets/{id}/balance/top-up")
    public String topUpWallet(@PathVariable UUID id) {

        Transaction transaction = walletService.addFunds(id, BigDecimal.valueOf(20));

        return "redirect:/transactions/" + transaction.getId();
    }

    @PutMapping("/wallets/{id}/status")
    public String switchWalletStatus(@PathVariable UUID id, @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        walletService.switchStatus(id, customUserDetails.getUserId());

        return "redirect:/wallets";
    }

}
