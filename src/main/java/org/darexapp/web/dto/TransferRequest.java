package org.darexapp.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class TransferRequest {

    @NotNull(message = "From wallet ID is required")
    private UUID WalletId;

    @NotNull(message = "Recipient username is required")
    private String toUsername;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

}
