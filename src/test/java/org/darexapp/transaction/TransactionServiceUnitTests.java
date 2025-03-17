package org.darexapp.transaction;

import org.darexapp.exception.DomainException;
import org.darexapp.transaction.model.Transaction;
import org.darexapp.transaction.model.TransactionStatus;
import org.darexapp.transaction.model.TransactionType;
import org.darexapp.transaction.repository.TransactionRepository;
import org.darexapp.transaction.service.TransactionService;
import org.darexapp.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceUnitTests {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void testCreateTransaction_success() {
        User owner = new User();
        owner.setUsername("testuser");
        String sender = "senderTest";
        String receiver = "receiverTest";
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal remainingBalance = new BigDecimal("200.00");
        Currency currency = Currency.getInstance("EUR");
        TransactionType type = TransactionType.DEPOSIT;
        TransactionStatus status = TransactionStatus.SUCCESSFUL;
        String description = "Test transaction";
        String failureReason = null;

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId(UUID.randomUUID());
            tx.setCreatedAt(LocalDateTime.now());
            return tx;
        });

        Transaction result = transactionService.createTransaction(
                owner, sender, receiver, amount, remainingBalance, currency, type, status, description, failureReason);

        assertNotNull(result.getId());
        assertEquals(owner, result.getOwner());
        assertEquals(sender, result.getSender());
        assertEquals(receiver, result.getReceiver());
        assertEquals(amount, result.getAmount());
        assertEquals(remainingBalance, result.getRemainingBalance());
        assertEquals(currency, result.getCurrency());
        assertEquals(type, result.getType());
        assertEquals(status, result.getStatus());
        assertEquals(description, result.getDescription());
        assertEquals(failureReason, result.getFailureReason());
        assertNotNull(result.getCreatedAt());

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void testGetAllTransactionsByOwnerId() {
        UUID ownerId = UUID.randomUUID();
        Transaction tx1 = Transaction.builder().id(UUID.randomUUID()).build();
        Transaction tx2 = Transaction.builder().id(UUID.randomUUID()).build();
        List<Transaction> transactions = List.of(tx1, tx2);

        when(transactionRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId)).thenReturn(transactions);

        List<Transaction> result = transactionService.getAllTransactionsByOwnerId(ownerId);
        assertEquals(2, result.size());
        assertEquals(tx1, result.get(0));
        assertEquals(tx2, result.get(1));

        verify(transactionRepository).findAllByOwnerIdOrderByCreatedAtDesc(ownerId);
    }

    @Test
    void testGetTransactionById_success() {
        UUID txId = UUID.randomUUID();
        Transaction tx = Transaction.builder().id(txId).build();

        when(transactionRepository.findById(txId)).thenReturn(Optional.of(tx));

        Transaction result = transactionService.getTransactionById(txId);
        assertEquals(tx, result);

        verify(transactionRepository).findById(txId);
    }

    @Test
    void testGetTransactionById_notFound() {
        UUID txId = UUID.randomUUID();
        when(transactionRepository.findById(txId)).thenReturn(Optional.empty());

        DomainException exception = assertThrows(DomainException.class,
                () -> transactionService.getTransactionById(txId));
        assertTrue(exception.getMessage().contains("Transaction not found"));

        verify(transactionRepository).findById(txId);
    }
}
