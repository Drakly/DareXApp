package org.darexapp.web.controller;

import jakarta.servlet.http.HttpSession;
import org.darexapp.security.CustomUserDetails;
import org.darexapp.transaction.model.Transaction;
import org.darexapp.transaction.service.TransactionService;
import org.darexapp.user.model.User;
import org.darexapp.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;

    @Autowired
    public TransactionController(TransactionService transactionService, UserService userService){
        this.transactionService = transactionService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView listTransactions(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        User user = userService.findById(customUserDetails.getUserId());
        List<Transaction> transactions = transactionService.getAllTransactionsByOwnerId(user.getId());


        ModelAndView mav = new ModelAndView("transactions");
        mav.addObject("transactions", transactions);
        return mav;
    }

    @GetMapping("/{transactionId}")
    public ModelAndView viewTransaction(@PathVariable("transactionId") UUID id) {
        Transaction transaction = transactionService.getTransactionById(id);
        ModelAndView mav = new ModelAndView("transaction-details"); // Thymeleaf шаблон: transactionDetails.html
        mav.addObject("transaction", transaction);
        return mav;
    }
}

