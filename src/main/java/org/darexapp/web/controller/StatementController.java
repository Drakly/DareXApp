package org.darexapp.web.controller;


import org.darexapp.security.CustomUserDetails;
import org.darexapp.transaction.model.Transaction;
import org.darexapp.transaction.service.TransactionService;
import org.darexapp.user.model.User;
import org.darexapp.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/statement")
public class StatementController {

    private final UserService userService;

    private final TransactionService transactionService;

    private final SpringTemplateEngine templateEngine;


    @Autowired
    public StatementController(UserService userService, TransactionService transactionService, SpringTemplateEngine templateEngine) {
        this.userService = userService;
        this.transactionService = transactionService;
        this.templateEngine = templateEngine;
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadStatementPdf(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) throws Exception {

        User currentUser = userService.findById(customUserDetails.getUserId());

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(1);

        List<Transaction> transactions = transactionService.getAllTransactionsByOwnerId(customUserDetails.getUserId());

        // Подготвяме контекста за Thymeleaf
        Context context = new Context();
        context.setVariable("user", currentUser);
        context.setVariable("startDate", startDate);
        context.setVariable("endDate", endDate);
        context.setVariable("transactions", transactions);

        String htmlContent = templateEngine.process("bank-statement", context);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(outputStream);
        renderer.finishPDF();

        byte[] pdfBytes = outputStream.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "bank-statement.pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }


}
