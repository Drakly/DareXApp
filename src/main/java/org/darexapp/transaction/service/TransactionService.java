package org.darexapp.transaction.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.darexapp.exception.DomainException;
import org.darexapp.transaction.model.Transaction;
import org.darexapp.transaction.model.TransactionStatus;
import org.darexapp.transaction.model.TransactionType;
import org.darexapp.transaction.repository.TransactionRepository;
import org.darexapp.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction createTransaction(
            User owner,
            String sender,
            String receiver,
            BigDecimal transactionAmount,
            BigDecimal balanceLeft,
            Currency currency,
            TransactionType type,
            TransactionStatus status,
            String description,
            String failureReason
    ) {

        Transaction transaction = Transaction.builder()
                .owner(owner)
                .sender(sender)
                .receiver(receiver)
                .amount(transactionAmount)
                .balanceLeft(balanceLeft)
                .currency(currency)
                .type(type)
                .status(status)
                .description(description)
                .failureReason(failureReason)
                .createdAt(LocalDateTime.now())
                .build();

        log.info("Transaction created: [{} -> {}], Amount: [{} {}], Status: [{}]", sender, receiver, transactionAmount, currency.getCurrencyCode(), status);

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getAllTransactionsByOwnerId(UUID ownerId) {
        log.info("Fetching all transactions for owner ID [{}]", ownerId);
        return transactionRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId);
    }


    public Transaction getTransactionById(UUID id) {
        return transactionRepository.findById(id).orElseThrow(() -> new DomainException("Transaction not found".formatted(id)));
    }
}
