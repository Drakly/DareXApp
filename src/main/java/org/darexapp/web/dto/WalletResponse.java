package org.darexapp.web.dto;

import lombok.Builder;
import lombok.Data;
import org.darexapp.wallet.model.WalletStatus;
import org.darexapp.wallet.model.WalletType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class WalletResponse {
    private UUID id;
    private BigDecimal balance;
    private String currency;
    private WalletStatus status;
    private WalletType type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 