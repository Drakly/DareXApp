package org.darexapp.wallet;

import org.darexapp.exception.DomainException;
import org.darexapp.transaction.model.Transaction;
import org.darexapp.transaction.model.TransactionStatus;
import org.darexapp.transaction.model.TransactionType;
import org.darexapp.transaction.service.TransactionService;
import org.darexapp.user.model.User;
import org.darexapp.wallet.model.Wallet;
import org.darexapp.wallet.model.WalletStatus;
import org.darexapp.wallet.model.WalletType;
import org.darexapp.wallet.repository.WalletRepository;
import org.darexapp.wallet.service.WalletService;
import org.darexapp.web.dto.TransferRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletServiceUnitTests {

    @Mock
    private WalletRepository walletRepository;
    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private WalletService walletService;

    @Test
    void testCreateWalletForUser_success() {
        User user = new User();
        user.setUsername("testuser");

        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> {
            Wallet w = invocation.getArgument(0);
            w.setId(UUID.randomUUID());
            return w;
        });

        Wallet createdWallet = walletService.createWalletForUser(user);
        assertNotNull(createdWallet.getId());
        assertEquals(new BigDecimal("20.00"), createdWallet.getBalance());
        assertEquals(WalletStatus.ACTIVE, createdWallet.getStatus());
        assertEquals(WalletType.STANDARD, createdWallet.getType());
    }

    @Test
    void testCreateInvestmentWallet_success() {
        User user = new User();
        user.setUsername("testuser");

        when(walletRepository.findAllByOwnerUsername("testuser"))
                .thenReturn(Collections.singletonList(new Wallet()));

        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> {
            Wallet w = invocation.getArgument(0);
            w.setId(UUID.randomUUID());
            return w;
        });

        assertDoesNotThrow(() -> walletService.createInvestmentWallet(user));
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    void testCreateInvestmentWallet_alreadyExists() {
        User user = new User();
        user.setUsername("testuser");

        List<Wallet> existingWallets = Arrays.asList(new Wallet(), new Wallet());
        when(walletRepository.findAllByOwnerUsername("testuser")).thenReturn(existingWallets);

        DomainException exception = assertThrows(DomainException.class,
                () -> walletService.createInvestmentWallet(user));
        assertTrue(exception.getMessage().contains("Investment wallet already exists"));
    }

    @Test
    void testFetchWalletById_success() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        Wallet fetchedWallet = walletService.fetchWalletById(walletId);
        assertEquals(walletId, fetchedWallet.getId());
    }

    @Test
    void testFetchWalletById_notFound() {
        UUID walletId = UUID.randomUUID();
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        DomainException exception = assertThrows(DomainException.class,
                () -> walletService.fetchWalletById(walletId));
        assertTrue(exception.getMessage().contains("Wallet with id"));
    }

    @Test
    void testGetSortedWalletsByOwnerId() {
        UUID ownerId = UUID.randomUUID();
        List<Wallet> wallets = Arrays.asList(new Wallet(), new Wallet());
        when(walletRepository.findAllByOwnerIdOrderByBalanceDesc(ownerId)).thenReturn(wallets);

        List<Wallet> result = walletService.getSortedWalletsByOwnerId(ownerId);
        assertEquals(2, result.size());
    }

    @Test
    void testSwitchStatus_success() {
        UUID walletId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setStatus(WalletStatus.ACTIVE);
        when(walletRepository.findByIdAndOwnerId(walletId, ownerId))
                .thenReturn(Optional.of(wallet));

        walletService.switchStatus(walletId, ownerId);
        assertEquals(WalletStatus.INACTIVE, wallet.getStatus());

        walletService.switchStatus(walletId, ownerId);
        assertEquals(WalletStatus.ACTIVE, wallet.getStatus());

        verify(walletRepository, times(2)).save(wallet);
    }

    @Test
    void testSwitchStatus_notFound() {
        UUID walletId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        when(walletRepository.findByIdAndOwnerId(walletId, ownerId))
                .thenReturn(Optional.empty());

        DomainException exception = assertThrows(DomainException.class,
                () -> walletService.switchStatus(walletId, ownerId));
        assertTrue(exception.getMessage().contains("Wallet not found or access denied"));
    }

    @Test
    void testAddFunds_success() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setStatus(WalletStatus.ACTIVE);
        wallet.setBalance(new BigDecimal("20.00"));
        wallet.setCurrency("EUR");
        User user = new User();
        user.setUsername("testuser");
        wallet.setOwner(user);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction successTx = new Transaction();
        successTx.setStatus(TransactionStatus.SUCCESSFUL);
        when(transactionService.createTransaction(
                any(User.class),
                anyString(),
                anyString(),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(Currency.class),
                any(TransactionType.class),
                eq(TransactionStatus.SUCCESSFUL),
                anyString(),
                isNull()
        )).thenReturn(successTx);

        Transaction result = walletService.addFunds(walletId, new BigDecimal("10.00"));
        assertEquals(TransactionStatus.SUCCESSFUL, result.getStatus());
        assertEquals(new BigDecimal("30.00"), wallet.getBalance());
    }

    @Test
    void testAddFunds_inactiveWallet() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setStatus(WalletStatus.INACTIVE);
        wallet.setBalance(new BigDecimal("20.00"));
        wallet.setCurrency("EUR");
        User user = new User();
        user.setUsername("testuser");
        wallet.setOwner(user);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        Transaction failedTx = new Transaction();
        failedTx.setStatus(TransactionStatus.FAILED);
        when(transactionService.createTransaction(
                any(User.class),
                anyString(),
                anyString(),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(Currency.class),
                any(TransactionType.class),
                eq(TransactionStatus.FAILED),
                anyString(),
                anyString()
        )).thenReturn(failedTx);

        Transaction result = walletService.addFunds(walletId, new BigDecimal("10.00"));
        assertEquals(TransactionStatus.FAILED, result.getStatus());
        assertEquals(new BigDecimal("20.00"), wallet.getBalance());
    }

    @Test
    void testDeductFunds_success() {
        UUID walletId = UUID.randomUUID();
        User user = new User();
        user.setUsername("testuser");
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setStatus(WalletStatus.ACTIVE);
        wallet.setBalance(new BigDecimal("50.00"));
        wallet.setCurrency("EUR");
        wallet.setOwner(user);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction successWithdrawal = new Transaction();
        successWithdrawal.setStatus(TransactionStatus.SUCCESSFUL);
        when(transactionService.createTransaction(
                any(User.class),
                anyString(),
                anyString(),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(Currency.class),
                any(TransactionType.class),
                eq(TransactionStatus.SUCCESSFUL),
                anyString(),
                isNull()
        )).thenReturn(successWithdrawal);

        Transaction result = walletService.deductFunds(user, walletId, new BigDecimal("20.00"), "Test deduction");
        assertEquals(TransactionStatus.SUCCESSFUL, result.getStatus());
        assertEquals(new BigDecimal("30.00"), wallet.getBalance());
    }

    @Test
    void testDeductFunds_insufficientFunds() {
        UUID walletId = UUID.randomUUID();
        User user = new User();
        user.setUsername("testuser");
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setStatus(WalletStatus.ACTIVE);
        wallet.setBalance(new BigDecimal("10.00"));
        wallet.setCurrency("EUR");
        wallet.setOwner(user);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        Transaction failedWithdrawal = new Transaction();
        failedWithdrawal.setStatus(TransactionStatus.FAILED);
        when(transactionService.createTransaction(
                any(User.class),
                anyString(),
                anyString(),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(Currency.class),
                any(TransactionType.class),
                eq(TransactionStatus.FAILED),
                anyString(),
                anyString()
        )).thenReturn(failedWithdrawal);

        Transaction result = walletService.deductFunds(user, walletId, new BigDecimal("20.00"), "Test deduction");
        assertEquals(TransactionStatus.FAILED, result.getStatus());
        assertEquals(new BigDecimal("10.00"), wallet.getBalance());
    }

    @Test
    void testDeductFunds_inactiveWallet() {
        UUID walletId = UUID.randomUUID();
        User user = new User();
        user.setUsername("testuser");
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setStatus(WalletStatus.INACTIVE);
        wallet.setBalance(new BigDecimal("50.00"));
        wallet.setCurrency("EUR");
        wallet.setOwner(user);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        Transaction failedWithdrawal = new Transaction();
        failedWithdrawal.setStatus(TransactionStatus.FAILED);
        when(transactionService.createTransaction(
                any(User.class),
                anyString(),
                anyString(),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(Currency.class),
                any(TransactionType.class),
                eq(TransactionStatus.FAILED),
                anyString(),
                anyString()
        )).thenReturn(failedWithdrawal);

        Transaction result = walletService.deductFunds(user, walletId, new BigDecimal("20.00"), "Test deduction");
        assertEquals(TransactionStatus.FAILED, result.getStatus());
        assertEquals(new BigDecimal("50.00"), wallet.getBalance());
    }

    @Test
    void testTransferFunds_receiverNotFound() {
        // Сетъп за изпращача
        User sender = new User();
        sender.setUsername("senderUser");
        Wallet senderWallet = new Wallet();
        senderWallet.setId(UUID.randomUUID());
        senderWallet.setStatus(WalletStatus.ACTIVE);
        senderWallet.setBalance(new BigDecimal("100.00"));
        senderWallet.setCurrency("EUR");
        senderWallet.setOwner(sender);

        TransferRequest transferRequest = TransferRequest.builder().build();
        transferRequest.setWalletId(senderWallet.getId());
        transferRequest.setToUsername("receiverUser");
        transferRequest.setAmount(new BigDecimal("50.00"));

        when(walletRepository.findById(senderWallet.getId())).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findAllByOwnerUsername("receiverUser")).thenReturn(Collections.emptyList());

        Transaction failedTransfer = new Transaction();
        failedTransfer.setStatus(TransactionStatus.FAILED);
        when(transactionService.createTransaction(
                any(User.class),
                anyString(),
                anyString(),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any(Currency.class),
                any(TransactionType.class),
                eq(TransactionStatus.FAILED),
                anyString(),
                anyString()
        )).thenReturn(failedTransfer);

        Transaction result = walletService.transferFunds(sender, transferRequest);
        assertEquals(TransactionStatus.FAILED, result.getStatus());
    }

    @Test
    void testTransferFunds_success() {
        WalletService spyWalletService = spy(walletService);

        User sender = new User();
        sender.setUsername("senderUser");
        Wallet senderWallet = new Wallet();
        senderWallet.setId(UUID.randomUUID());
        senderWallet.setStatus(WalletStatus.ACTIVE);
        senderWallet.setBalance(new BigDecimal("100.00"));
        senderWallet.setCurrency("EUR");
        senderWallet.setOwner(sender);

        User receiver = new User();
        receiver.setUsername("receiverUser");
        Wallet receiverWallet = new Wallet();
        receiverWallet.setId(UUID.randomUUID());
        receiverWallet.setStatus(WalletStatus.ACTIVE);
        receiverWallet.setType(WalletType.STANDARD);
        receiverWallet.setBalance(new BigDecimal("20.00"));
        receiverWallet.setCurrency("EUR");
        receiverWallet.setOwner(receiver);

        TransferRequest transferRequest = TransferRequest.builder().build();
        transferRequest.setWalletId(senderWallet.getId());
        transferRequest.setToUsername("receiverUser");
        transferRequest.setAmount(new BigDecimal("50.00"));

        doReturn(senderWallet).when(spyWalletService).fetchWalletById(senderWallet.getId());
        when(walletRepository.findAllByOwnerUsername("receiverUser"))
                .thenReturn(List.of(receiverWallet));

        Transaction successfulWithdrawal = new Transaction();
        successfulWithdrawal.setStatus(TransactionStatus.SUCCESSFUL);
        doReturn(successfulWithdrawal).when(spyWalletService).deductFunds(eq(sender),
                eq(senderWallet.getId()), eq(transferRequest.getAmount()), anyString());

        BigDecimal receiverInitialBalance = receiverWallet.getBalance();

        Transaction result = spyWalletService.transferFunds(sender, transferRequest);
        assertEquals(TransactionStatus.SUCCESSFUL, result.getStatus());

        verify(walletRepository, atLeastOnce()).save(argThat(w ->
                w.getId().equals(receiverWallet.getId()) &&
                        w.getBalance().compareTo(receiverInitialBalance.add(transferRequest.getAmount())) == 0
        ));
    }
}
