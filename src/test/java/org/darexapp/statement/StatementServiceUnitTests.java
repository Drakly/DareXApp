package org.darexapp.statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.darexapp.exception.DomainException;
import org.darexapp.transaction.service.TransactionService;
import org.darexapp.user.model.User;
import org.darexapp.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class StatementServiceUnitTests {

    @Mock
    private UserService userService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private TemplateEngine templateEngine;
    @InjectMocks
    private StatementService statementService;

    @Test
    void testGenerateBankStatementPdf_success() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setUsername("TestUser");
        when(userService.findById(userId)).thenReturn(user);
        when(transactionService.getAllTransactionsByOwnerId(userId)).thenReturn(Collections.emptyList());
        String htmlContent = "<html><body>Bank Statement</body></html>";
        when(templateEngine.process(eq("bank-statement"), any(Context.class))).thenReturn(htmlContent);

        byte[] pdfBytes = statementService.generateBankStatementPdf(userId);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("bank-statement"), contextCaptor.capture());
        Context capturedContext = contextCaptor.getValue();
        assertEquals(user, capturedContext.getVariable("user"));
        assertNotNull(capturedContext.getVariable("startDate"));
        assertNotNull(capturedContext.getVariable("endDate"));
        assertNotNull(capturedContext.getVariable("transactions"));
    }

    @Test
    void testGenerateBankStatementPdf_userNotFound() {
        UUID userId = UUID.randomUUID();
        when(userService.findById(userId)).thenThrow(new DomainException("User not found"));

        DomainException ex = assertThrows(DomainException.class,
                () -> statementService.generateBankStatementPdf(userId));
        assertTrue(ex.getMessage().contains("User not found"));
    }
}
