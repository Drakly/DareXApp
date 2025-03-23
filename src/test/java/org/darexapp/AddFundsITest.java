package org.darexapp;

import org.darexapp.transaction.model.Transaction;
import org.darexapp.transaction.model.TransactionStatus;
import org.darexapp.user.model.Country;
import org.darexapp.user.model.User;
import org.darexapp.user.service.UserService;
import org.darexapp.wallet.model.Wallet;
import org.darexapp.wallet.service.WalletService;
import org.darexapp.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AddFundsITest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private UserService userService;

    @Test
    public void testAddFunds_SuccessfulDeposit() {
        RegisterRequest registerUser = new RegisterRequest();
        registerUser.setUsername("Kris123");
        registerUser.setPassword("Kris123@");
        registerUser.setEmail("kris1234@abv.bg");
        registerUser.setCountry(Country.BULGARIA);

        User user = userService.register(registerUser);


        BigDecimal initialBalance = user.getWallets().get(0).getBalance();
        BigDecimal depositAmount = new BigDecimal("20.00");

        Transaction transaction = walletService.addFunds(user.getWallets().get(0).getId(), depositAmount);

        Wallet updatedWallet = walletService.fetchWalletById(user.getWallets().get(0).getId());

        assertEquals(initialBalance.add(depositAmount), updatedWallet.getBalance());
        assertEquals(TransactionStatus.SUCCESSFUL, transaction.getStatus());
    }

//    @Test
//    public void testAddFunds_FailedDeposit_InactiveWallet() {
//        RegisterRequest registerUser = new RegisterRequest();
//        registerUser.setUsername("DarklyG");
//        registerUser.setPassword("Kris123@");
//        registerUser.setEmail("darexapp123@gmail.com");
//        registerUser.setCountry(Country.BULGARIA);
//
//        User user = userService.register(registerUser);
//
//        Wallet wallet = walletService.createWalletForUser(user);
//
//        walletService.switchStatus(wallet.getId(), user.getId());
//        Wallet inactiveWallet = walletService.fetchWalletById(wallet.getId());
//        assertEquals(WalletStatus.INACTIVE, inactiveWallet.getStatus());
//
//        BigDecimal depositAmount = new BigDecimal("30.00");
//
//        Transaction transaction = walletService.addFunds(wallet.getId(), depositAmount);
//
//        Wallet updatedWallet = walletService.fetchWalletById(wallet.getId());
//        assertEquals(inactiveWallet.getBalance(), updatedWallet.getBalance());
//
//        assertEquals(TransactionStatus.FAILED, transaction.getStatus());
//    }
}
