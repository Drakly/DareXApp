package org.darexapp.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class InvestmentWalletRequest {

    @NotNull
    private UUID standardWalletId;

    @NotNull
    private UUID investmentWalletId;

    @NotNull
    @Positive
    private BigDecimal amount;
}
