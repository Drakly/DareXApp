package org.darexapp;

import org.darexapp.transaction.model.Transaction;
import org.darexapp.transaction.model.TransactionStatus;
import org.darexapp.user.model.Country;
import org.darexapp.user.model.User;
import org.darexapp.user.service.UserService;
import org.darexapp.wallet.model.Wallet;
import org.darexapp.wallet.model.WalletStatus;
import org.darexapp.wallet.repository.WalletRepository;
import org.darexapp.wallet.service.WalletService;
import org.darexapp.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
public class AddFundsITest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @Test
    public void testAddFundsSuccessful() {
        Wallet wallet = Wallet.builder()
                .balance(new BigDecimal("100.00"))
                .status(WalletStatus.ACTIVE)
                .currency(Currency.getInstance("EUR").getCurrencyCode())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        wallet = walletRepository.save(wallet);

        BigDecimal amountToAdd = new BigDecimal("50.00");

        Transaction transaction = walletService.addFunds(wallet.getId(), amountToAdd);

        assertNotNull(transaction, "Transaction must not be null");
        assertEquals(TransactionStatus.SUCCESSFUL, transaction.getStatus(), "The status must be SUCCESSFUL");

        Wallet updatedWallet = walletRepository.findById(wallet.getId()).orElse(null);
        assertNotNull(updatedWallet, "The updated wallet must be found");
        assertEquals(new BigDecimal("150.00"), updatedWallet.getBalance(), "The balance must be equal to 150.00");
    }

    @Test
    public void testAddFundsWalletInactive() {
        Wallet wallet = Wallet.builder()
                .balance(new BigDecimal("100.00"))
                .status(WalletStatus.INACTIVE)
                .currency(Currency.getInstance("EUR").getCurrencyCode())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        wallet = walletRepository.save(wallet);

        BigDecimal amountToAdd = new BigDecimal("50.00");

        Transaction transaction = walletService.addFunds(wallet.getId(), amountToAdd);

        assertNotNull(transaction, "Transaction must not be null");
        assertEquals(TransactionStatus.FAILED, transaction.getStatus(), "Status must be FAILED");
        assertEquals("Wallet is inactive", transaction.getFailureReason(), "Wallet is inactive");

        Wallet updatedWallet = walletRepository.findById(wallet.getId()).orElse(null);
        assertNotNull(updatedWallet, "Updated wallet must be found");
        assertEquals(new BigDecimal("100.00"), updatedWallet.getBalance(), "Balance must be equal to 100.00");
    }
}
