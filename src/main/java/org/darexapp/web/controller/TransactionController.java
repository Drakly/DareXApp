package org.darexapp.web.controller;

import org.darexapp.transaction.model.Transaction;
import org.darexapp.transaction.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public TransactionController(TransactionService transactionService){
        this.transactionService = transactionService;
    }

    // Показва всички транзакции за даден owner
    @GetMapping
    public ModelAndView listTransactions(UUID ownerId) {
        List<Transaction> transactions = transactionService.getAllTransactionsByOwnerId(ownerId);
        ModelAndView mav = new ModelAndView("transactions"); // Thymeleaf шаблон: transactions.html
        mav.addObject("transactions", transactions);
        mav.addObject("ownerId", ownerId);
        return mav;
    }

    // Показва детайлите на конкретна транзакция по ID
    @GetMapping("/{transactionId}")
    public ModelAndView viewTransaction(@PathVariable("transactionId") UUID id) {
        Transaction transaction = transactionService.getTransactionById(id);
        ModelAndView mav = new ModelAndView("transaction-details"); // Thymeleaf шаблон: transactionDetails.html
        mav.addObject("transaction", transaction);
        return mav;
    }
}

