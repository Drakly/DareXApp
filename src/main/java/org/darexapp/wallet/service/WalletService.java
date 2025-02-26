package org.darexapp.wallet.service;


import lombok.extern.slf4j.Slf4j;
import org.darexapp.exception.DomainException;
import org.darexapp.transaction.model.Transaction;
import org.darexapp.transaction.model.TransactionStatus;
import org.darexapp.transaction.model.TransactionType;
import org.darexapp.transaction.repository.TransactionRepository;
import org.darexapp.transaction.service.TransactionService;
import org.darexapp.user.model.User;
import org.darexapp.wallet.model.Wallet;
import org.darexapp.wallet.model.WalletStatus;
import org.darexapp.wallet.model.WalletType;
import org.darexapp.wallet.repository.WalletRepository;
import org.darexapp.web.dto.TransferRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class WalletService {

    private static final String WALLET_PROVIDER_NAME = "DareX Wallet CO.";

    private final WalletRepository walletRepository;
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
//    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public WalletService(WalletRepository walletRepository,
                         TransactionService transactionService,
                         ApplicationEventPublisher eventPublisher, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionService = transactionService;
//        this.eventPublisher = eventPublisher;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Създава нов портфейл за даден потребител.
     */
    public Wallet createWalletForUser(User user) {
        Wallet newWallet = buildInitialWallet(user);
        Wallet savedWallet = walletRepository.save(newWallet);
        log.info("Created wallet with id [{}] and initial balance {:.2f}.",
                savedWallet.getId(), savedWallet.getBalance());
        return savedWallet;
    }

    @Transactional
    public Transaction processSubscriptionCharge(User user, UUID walletId, BigDecimal amount, String description) {
        Wallet wallet = fetchWalletById(walletId);
        String errorMsg = null;
        boolean chargeFailed = false;

        // Verify wallet status and funds
        if (wallet.getStatus() == WalletStatus.INACTIVE) {
            errorMsg = "Wallet is inactive";
            chargeFailed = true;
        }
        if (wallet.getBalance().compareTo(amount) < 0) {
            errorMsg = "Insufficient funds";
            chargeFailed = true;
        }

        // If failure, record a failed transaction
        if (chargeFailed) {
            return transactionService.createTransaction(
                    user,
                    wallet.getId().toString(),
                    WALLET_PROVIDER_NAME,
                    amount,
                    wallet.getBalance(),
                    Currency.getInstance(wallet.getCurrency()),
                    TransactionType.WITHDRAWAL,
                    TransactionStatus.FAILED,
                    description,
                    errorMsg
            );
        }

        // Deduct the amount and update the wallet
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        // Build and optionally publish a payment notification event
//        PaymentNotificationEvent event = PaymentNotificationEvent.builder()
//                .userId(user.getId())
//                .paymentTime(LocalDateTime.now())
//                .email(user.getEmail())
//                .amount(amount)
//                .build();
        // eventPublisher.publishEvent(event);

        return transactionService.createTransaction(
                user,
                wallet.getId().toString(),
                WALLET_PROVIDER_NAME,
                amount,
                wallet.getBalance(),
                Currency.getInstance(wallet.getCurrency()),
                TransactionType.WITHDRAWAL,
                TransactionStatus.SUCCESSFUL,
                description,
                null
        );
    }


    @Transactional
    public Transaction addFunds(UUID walletId, BigDecimal amount) {
        Wallet wallet = fetchWalletById(walletId);
        String description = String.format("Top-up of %.2f", amount.doubleValue());

        if (wallet.getStatus() == WalletStatus.INACTIVE) {
            return transactionService.createTransaction(
                    wallet.getOwner(),
                    WALLET_PROVIDER_NAME,
                    walletId.toString(),
                    amount,
                    wallet.getBalance(),
                    Currency.getInstance("EUR"),
                    TransactionType.DEPOSIT,
                    TransactionStatus.FAILED,
                    description,
                    "Wallet is inactive"
            );
        }

        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        return transactionService.createTransaction(
                wallet.getOwner(),
                WALLET_PROVIDER_NAME,
                walletId.toString(),
                amount,
                wallet.getBalance(),
                Currency.getInstance("EUR"),
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCESSFUL,
                description,
                null
        );
    }


    @Transactional
    public Transaction transferFunds(User sender, TransferRequest transferRequest) {
        Wallet senderWallet = fetchWalletById(transferRequest.getWalletId());
        Optional<Wallet> receiverWalletOpt = walletRepository.findAllByOwnerUsername(transferRequest.getToUsername())
                .stream()
                .filter(w -> w.getStatus() == WalletStatus.ACTIVE)
                .filter(wallet -> wallet.getType() == WalletType.STANDARD)
                .findFirst();


        String transferDesc = String.format("Transfer from %s to %s of %.2f",
                sender.getUsername(), transferRequest.getToUsername(), transferRequest.getAmount());

        if (receiverWalletOpt.isEmpty()) {
            return transactionService.createTransaction(
                    sender,
                    senderWallet.getId().toString(),
                    transferRequest.getToUsername(),
                    transferRequest.getAmount(),
                    senderWallet.getBalance(),
                    Currency.getInstance("EUR"),
                    TransactionType.WITHDRAWAL,
                    TransactionStatus.FAILED,
                    transferDesc,
                    "Receiver wallet not found or inactive"
            );
        }

        // Първо опитваме да изтеглим средствата от изпращащия портфейл
        Transaction withdrawalTx = deductFunds(sender, senderWallet.getId(), transferRequest.getAmount(), transferDesc);
        if (withdrawalTx.getStatus() == TransactionStatus.FAILED) {
            return withdrawalTx;
        }

        // Ако тегленето е успешно, добавяме средствата към портфейла на получателя
        Wallet receiverWallet = receiverWalletOpt.get();
        receiverWallet.setBalance(receiverWallet.getBalance().add(transferRequest.getAmount()));
        receiverWallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(receiverWallet);

        // Създаваме успешна депозитна транзакция за получателя
        transactionService.createTransaction(
                receiverWallet.getOwner(),
                senderWallet.getId().toString(),
                receiverWallet.getId().toString(),
                transferRequest.getAmount(),
                receiverWallet.getBalance(),
                Currency.getInstance("EUR"),
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCESSFUL,
                transferDesc,
                null
        );

        return withdrawalTx;
    }

    /**
     * Тегли средства от даден портфейл.
     * Ако портфейлът е неактивен или няма достатъчно средства, връща неуспешна транзакция.
     */
    @Transactional
    public Transaction deductFunds(User user, UUID walletId, BigDecimal amount, String description) {
        Wallet wallet = fetchWalletById(walletId);

        String errorMsg = null;
        boolean failure = false;

        if (wallet.getStatus() == WalletStatus.INACTIVE) {
            errorMsg = "Wallet inactive";
            failure = true;
        }
        if (wallet.getBalance().compareTo(amount) < 0) {
            errorMsg = "Insufficient funds";
            failure = true;
        }

        if (failure) {
            return transactionService.createTransaction(
                    user,
                    wallet.getId().toString(),
                    WALLET_PROVIDER_NAME,
                    amount,
                    wallet.getBalance(),
                    Currency.getInstance("EUR"),
                    TransactionType.WITHDRAWAL,
                    TransactionStatus.FAILED,
                    description,
                    errorMsg
            );
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        // Публикуваме евент за успешното плащане (ако е необходимо)
        // Ако желаете, разкоментирайте следния ред:
        // eventPublisher.publishEvent(event);

        return transactionService.createTransaction(
                user,
                wallet.getId().toString(),
                WALLET_PROVIDER_NAME,
                amount,
                wallet.getBalance(),
                Currency.getInstance("EUR"),
                TransactionType.WITHDRAWAL,
                TransactionStatus.SUCCESSFUL,
                description,
                null
        );
    }

    @Transactional
    public void createInvestmentWallet(User user) {
        List<Wallet> allWallets = walletRepository.findAllByOwnerUsername(user.getUsername());

         boolean isUserHaveInvestmentWallet = allWallets.size() == 2;

         if (isUserHaveInvestmentWallet) {
             throw new DomainException("Investment wallet already exists");
         }

        Wallet investmentWallet = Wallet.builder()
                .owner(user)
                .balance(new BigDecimal("0"))
                .type(WalletType.INVESTMENT)
                .status(WalletStatus.ACTIVE)
                .currency(String.valueOf(Currency.getInstance("EUR")))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        walletRepository.save(investmentWallet);
    }
    @Transactional
    public void transferToInvestment(User user, UUID standardWalletId, UUID investmentWalletId, BigDecimal amount) {
        Wallet standardWallet = fetchWalletById(standardWalletId);
        Wallet investmentWallet = fetchWalletById(investmentWalletId);

        // Validate that both wallets belong to the user
        if (!standardWallet.getOwner().getId().equals(user.getId()) ||
                !investmentWallet.getOwner().getId().equals(user.getId())) {
            throw new DomainException("Wallets do not belong to the user");
        }

        if (standardWallet.getType() != WalletType.STANDARD) {
            throw new DomainException("Source wallet must be a standard wallet");
        }
        if (investmentWallet.getType() != WalletType.INVESTMENT) {
            throw new DomainException("Target wallet must be an investment wallet");
        }

        // Ensure sufficient funds in the standard wallet
        if (standardWallet.getBalance().compareTo(amount) < 0) {
            throw new DomainException("Insufficient funds in the standard wallet");
        }

        // Deduct funds from the standard wallet
        standardWallet.setBalance(standardWallet.getBalance().subtract(amount));
        standardWallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(standardWallet);

        // Add funds to the investment wallet
        investmentWallet.setBalance(investmentWallet.getBalance().add(amount));
        investmentWallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(investmentWallet);

        // Record the transfer as a transaction
        Transaction transferTX = Transaction.builder()
                .owner(user)
                .amount(amount)
                .description("Transfer from Standard to Investment Wallet")
                .createdAt(LocalDateTime.now())
                .build();

    }

    public Wallet fetchWalletById(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new DomainException(String.format("Wallet with id [%s] does not exist.", walletId)));
    }

    public List<Wallet> getSortedWalletsByOwnerId(UUID ownerId) {
        return walletRepository.findAllByOwnerIdOrderByBalanceDesc(ownerId);
    }

    private Wallet buildInitialWallet(User user) {
        return Wallet.builder()
                .owner(user)
                .status(WalletStatus.ACTIVE)
                .type(WalletType.STANDARD)
                .balance(new BigDecimal("20.00"))
                .currency(String.valueOf(Currency.getInstance("EUR")))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

}
