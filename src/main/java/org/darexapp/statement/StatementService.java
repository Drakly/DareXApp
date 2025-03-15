package org.darexapp.statement;

import org.darexapp.transaction.model.Transaction;
import org.darexapp.transaction.service.TransactionService;
import org.darexapp.user.model.User;
import org.darexapp.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class StatementService {

    private final UserService userService;
    private final TransactionService transactionService;
    private final TemplateEngine templateEngine;

    @Autowired
    public StatementService(UserService userService, TransactionService transactionService, TemplateEngine templateEngine) {
        this.userService = userService;
        this.transactionService = transactionService;
        this.templateEngine = templateEngine;
    }

    public byte[] generateBankStatementPdf(UUID userId) throws Exception {
        User currentUser = userService.findById(userId);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(1);

        List<Transaction> transactions = transactionService.getAllTransactionsByOwnerId(userId);

        Context context = new Context();
        context.setVariable("user", currentUser);
        context.setVariable("startDate", startDate);
        context.setVariable("endDate", endDate);
        context.setVariable("transactions", transactions);

        String htmlContent = templateEngine.process("bank-statement", context);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);
            renderer.finishPDF();
            return outputStream.toByteArray();
        }
    }
}
