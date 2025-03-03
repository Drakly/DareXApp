package org.darexapp.web.dto;

import lombok.Builder;
import lombok.Data;
import org.darexapp.transaction.model.TransactionStatus;
import org.darexapp.transaction.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.UUID;

@Data
@Builder
public class TransactionResponse {
    private UUID id;
    private String sender;
    private String receiver;
    private BigDecimal amount;
    private BigDecimal balanceLeft;
    private Currency currency;
    private TransactionType type;
    private TransactionStatus status;
    private String description;
    private String failureReason;
    private LocalDateTime createdAt;
} 