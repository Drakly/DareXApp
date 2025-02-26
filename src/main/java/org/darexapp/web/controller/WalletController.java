package org.darexapp.web.controller;

import jakarta.validation.Valid;
import org.darexapp.security.CustomUserDetails;
import org.darexapp.transaction.model.Transaction;
import org.darexapp.transaction.service.TransactionService;
import org.darexapp.user.model.User;
import org.darexapp.user.service.UserService;
import org.darexapp.wallet.model.Wallet;
import org.darexapp.wallet.model.WalletType;
import org.darexapp.wallet.repository.WalletRepository;
import org.darexapp.wallet.service.WalletService;
import org.darexapp.web.dto.InvestmentWalletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class WalletController {

    private final UserService userService;// Предполага се, че имаш UserService
    private final WalletService walletService;
    private final TransactionService transactionService;
    private final WalletRepository walletRepository;

    @Autowired
    public WalletController(UserService userService, WalletService walletService, TransactionService transactionService, WalletRepository walletRepository) {
        this.userService = userService;
        this.walletService = walletService;
        this.transactionService = transactionService;
        this.walletRepository = walletRepository;
    }

    @GetMapping("/wallets")
    public ModelAndView getWallets(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        User user = userService.findById(customUserDetails.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("wallet");
        modelAndView.addObject("user", user);

        List<Transaction> transactions = transactionService.getAllTransactionsByOwnerId(user.getId());
        List<Transaction> recentTransactions = transactions.stream().limit(5).collect(Collectors.toList());
        modelAndView.addObject("recentTransactions", recentTransactions);

//        List<Wallet> userWallets = walletRepository.findAllByOwnerId(user.getId());
//        modelAndView.addObject("wallets", userWallets);

        List<Wallet> sortedWallets = walletService.getSortedWalletsByOwnerId(user.getId());
        modelAndView.addObject("sortedWallets", sortedWallets);
        return modelAndView;
    }

    @PostMapping("/investment")
    public String processInvestmentTransfer(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @ModelAttribute("investmentWalletRequest") InvestmentWalletRequest investmentWalletRequest,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "create-investment-wallet";
        }

        User user = userService.findById(customUserDetails.getUserId());

        walletService.transferToInvestment(
                user,
                investmentWalletRequest.getStandardWalletId(),
                investmentWalletRequest.getInvestmentWalletId(),
                investmentWalletRequest.getAmount()
        );

        return "redirect:/wallets";
    }

    @PostMapping("/wallets/create-investment")
    public String createInvestmentWallet(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        User user = userService.findById(customUserDetails.getUserId());
        walletService.createInvestmentWallet(user);

        return "redirect:/wallets";
    }
}
