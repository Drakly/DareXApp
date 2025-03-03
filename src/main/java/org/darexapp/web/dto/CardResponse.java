package org.darexapp.web.dto;

import lombok.Builder;
import lombok.Data;
import org.darexapp.card.model.CardType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CardResponse {
    private UUID id;
    private CardType cardType;
    private String cardNumber;
    private String cardHolderName;
    private LocalDateTime expiryDate;
    private LocalDateTime createdOn;
    private UUID walletId;
} 