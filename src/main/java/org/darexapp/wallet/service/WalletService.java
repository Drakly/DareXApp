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
    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("20.00");
    private static final Currency DEFAULT_CURRENCY = Currency.getInstance("EUR");

    private final WalletRepository walletRepository;
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;

    @Autowired
    public WalletService(WalletRepository walletRepository,
                         TransactionService transactionService,
                         TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionService = transactionService;
//        this.eventPublisher = eventPublisher;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Creates a new standard wallet for a user with initial balance.
     *
     * @param user The user to create the wallet for
     * @return The newly created wallet
     */
    @Transactional
    public Wallet createWalletForUser(User user) {
        Wallet newWallet = buildInitialWallet(user);
        Wallet savedWallet = walletRepository.save(newWallet);
        
        log.info("Created wallet with id [{}] and initial balance {:.2f}",
                savedWallet.getId(), savedWallet.getBalance());
                
        return savedWallet;
    }

    /**
     * Creates an investment wallet for a user if they don't already have one.
     *
     * @param user The user to create the investment wallet for
     * @throws DomainException if user already has an investment wallet
     */
    @Transactional
    public void createInvestmentWallet(User user) {
        validateInvestmentWalletCreation(user);

        Wallet investmentWallet = buildInvestmentWallet(user);
        walletRepository.save(investmentWallet);
        
        log.info("Created investment wallet for user [{}]", user.getUsername());
    }

    /**
     * Retrieves a wallet by its ID.
     *
     * @param walletId The ID of the wallet to retrieve
     * @return The found wallet
     * @throws DomainException if wallet not found
     */
    @Transactional(readOnly = true)
    public Wallet fetchWalletById(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new DomainException(
                        String.format("Wallet with id [%s] does not exist.", walletId)
                ));
    }

    /**
     * Retrieves all wallets for a user, sorted by balance in descending order.
     *
     * @param ownerId The ID of the wallet owner
     * @return List of sorted wallets
     */
    @Transactional(readOnly = true)
    public List<Wallet> getSortedWalletsByOwnerId(UUID ownerId) {
        return walletRepository.findAllByOwnerIdOrderByBalanceDesc(ownerId);
    }

    /**
     * Toggles the status of a wallet between ACTIVE and INACTIVE.
     *
     * @param walletId The ID of the wallet to toggle
     * @param ownerId The ID of the wallet owner
     * @throws DomainException if wallet not found or doesn't belong to owner
     */
    @Transactional
    public void switchStatus(UUID walletId, UUID ownerId) {
        Wallet wallet = walletRepository.findByIdAndOwnerId(walletId, ownerId)
                .orElseThrow(() -> new DomainException("Wallet not found or access denied"));

        wallet.setStatus(wallet.getStatus() == WalletStatus.ACTIVE ? 
                WalletStatus.INACTIVE : WalletStatus.ACTIVE);
                
        walletRepository.save(wallet);
        log.info("Switched wallet [{}] status to [{}]", walletId, wallet.getStatus());
    }

    // Private helper methods

    private void validateInvestmentWalletCreation(User user) {
        List<Wallet> allWallets = walletRepository.findAllByOwnerUsername(user.getUsername());
        boolean hasInvestmentWallet = allWallets.size() == 2;

        if (hasInvestmentWallet) {
            throw new DomainException("Investment wallet already exists");
        }
    }

    private Wallet buildInitialWallet(User user) {
        LocalDateTime now = LocalDateTime.now();
        return Wallet.builder()
                .owner(user)
                .status(WalletStatus.ACTIVE)
                .type(WalletType.STANDARD)
                .balance(INITIAL_BALANCE)
                .currency(DEFAULT_CURRENCY.getCurrencyCode())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private Wallet buildInvestmentWallet(User user) {
        LocalDateTime now = LocalDateTime.now();
        return Wallet.builder()
                .owner(user)
                .balance(BigDecimal.ZERO)
                .type(WalletType.INVESTMENT)
                .status(WalletStatus.ACTIVE)
                .currency(DEFAULT_CURRENCY.getCurrencyCode())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Transactional
    public Transaction addFunds(UUID walletId, BigDecimal amount) {
        Wallet wallet = fetchWalletById(walletId);
        String description = createTopUpDescription(amount);

        if (wallet.getStatus() == WalletStatus.INACTIVE) {
            return createFailedDepositTransaction(wallet, amount, description);
        }

        updateWalletBalanceForDeposit(wallet, amount);
        return createSuccessfulDepositTransaction(wallet, amount, description);
    }

    private String createTopUpDescription(BigDecimal amount) {
        return String.format("Top-up of %.2f", amount.doubleValue());
    }

    private void updateWalletBalanceForDeposit(Wallet wallet, BigDecimal amount) {
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);
    }

    private Transaction createFailedDepositTransaction(Wallet wallet, BigDecimal amount, String description) {
        return transactionService.createTransaction(
                wallet.getOwner(),
                WALLET_PROVIDER_NAME,
                wallet.getId().toString(),
                amount,
                wallet.getBalance(),
                Currency.getInstance("EUR"),
                TransactionType.DEPOSIT,
                TransactionStatus.FAILED,
                description,
                "Wallet is inactive"
        );
    }

    private Transaction createSuccessfulDepositTransaction(Wallet wallet, BigDecimal amount, String description) {
        return transactionService.createTransaction(
                wallet.getOwner(),
                WALLET_PROVIDER_NAME,
                wallet.getId().toString(),
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
        Optional<Wallet> receiverWalletOpt = findActiveStandardWallet(transferRequest.getToUsername());
        String transferDesc = createTransferDescription(sender.getUsername(), 
                transferRequest.getToUsername(), transferRequest.getAmount());

        if (receiverWalletOpt.isEmpty()) {
            return createFailedTransferTransaction(sender, senderWallet, 
                    transferRequest.getToUsername(), transferRequest.getAmount(), transferDesc);
        }

        Transaction withdrawalTx = deductFunds(sender, senderWallet.getId(), 
                transferRequest.getAmount(), transferDesc);
        if (withdrawalTx.getStatus() == TransactionStatus.FAILED) {
            return withdrawalTx;
        }

        Wallet receiverWallet = receiverWalletOpt.get();
        processReceiverWallet(receiverWallet, transferRequest.getAmount(), 
                sender, senderWallet, transferDesc);

        return withdrawalTx;
    }

    private Optional<Wallet> findActiveStandardWallet(String username) {
        return walletRepository.findAllByOwnerUsername(username)
                .stream()
                .filter(w -> w.getStatus() == WalletStatus.ACTIVE)
                .filter(wallet -> wallet.getType() == WalletType.STANDARD)
                .findFirst();
    }

    private String createTransferDescription(String senderUsername, String receiverUsername, BigDecimal amount) {
        return String.format("Transfer from %s to %s of %.2f",
                senderUsername, receiverUsername, amount);
    }

    private Transaction createFailedTransferTransaction(User sender, Wallet senderWallet, 
            String receiverUsername, BigDecimal amount, String description) {
        return transactionService.createTransaction(
                sender,
                senderWallet.getId().toString(),
                receiverUsername,
                amount,
                senderWallet.getBalance(),
                Currency.getInstance("EUR"),
                TransactionType.WITHDRAWAL,
                TransactionStatus.FAILED,
                description,
                "Receiver wallet not found or inactive"
        );
    }

    private void processReceiverWallet(Wallet receiverWallet, BigDecimal amount, 
            User sender, Wallet senderWallet, String description) {
        receiverWallet.setBalance(receiverWallet.getBalance().add(amount));
        receiverWallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(receiverWallet);

        transactionService.createTransaction(
                receiverWallet.getOwner(),
                senderWallet.getId().toString(),
                receiverWallet.getId().toString(),
                amount,
                receiverWallet.getBalance(),
                Currency.getInstance("EUR"),
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCESSFUL,
                description,
                null
        );
    }

    /**
     * Тегли средства от даден портфейл.
     * Ако портфейлът е неактивен или няма достатъчно средства, връща неуспешна транзакция.
     */
    @Transactional
    public Transaction deductFunds(User user, UUID walletId, BigDecimal amount, String description) {
        Wallet wallet = fetchWalletById(walletId);
        
        ValidationResult validationResult = validateDeduction(wallet, amount);
        if (!validationResult.isValid()) {
            return createFailedWithdrawalTransaction(user, wallet, amount, description, 
                    validationResult.getErrorMessage());
        }

        updateWalletBalance(wallet, amount);
        return createSuccessfulWithdrawalTransaction(user, wallet, amount, description);
    }

    private ValidationResult validateDeduction(Wallet wallet, BigDecimal amount) {
        if (wallet.getStatus() == WalletStatus.INACTIVE) {
            return new ValidationResult(false, "Wallet inactive");
        }
        if (wallet.getBalance().compareTo(amount) < 0) {
            return new ValidationResult(false, "Insufficient funds");
        }
        return new ValidationResult(true, null);
    }

    private void updateWalletBalance(Wallet wallet, BigDecimal amount) {
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);
    }

    private Transaction createFailedWithdrawalTransaction(User user, Wallet wallet, 
            BigDecimal amount, String description, String errorMessage) {
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
                errorMessage
        );
    }

    private Transaction createSuccessfulWithdrawalTransaction(User user, Wallet wallet, 
            BigDecimal amount, String description) {
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

    // Helper class for validation results
    private static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        boolean isValid() {
            return valid;
        }

        String getErrorMessage() {
            return errorMessage;
        }
    }
}
