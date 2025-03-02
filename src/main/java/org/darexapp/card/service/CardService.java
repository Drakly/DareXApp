package org.darexapp.card.service;

import lombok.extern.slf4j.Slf4j;
import org.darexapp.card.model.Card;
import org.darexapp.card.model.CardType;
import org.darexapp.card.repository.CardRepository;
import org.darexapp.user.model.User;
import org.darexapp.user.service.UserService;
import org.darexapp.wallet.model.Wallet;
import org.darexapp.wallet.model.WalletStatus;
import org.darexapp.wallet.repository.WalletRepository;
import org.darexapp.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CardService {

    private final CardRepository cardRepository;
    private final WalletService walletService;
    private final WalletRepository walletRepository;

    @Autowired
    public CardService(CardRepository cardRepository, WalletService walletService, WalletRepository walletRepository) {
        this.cardRepository = cardRepository;
        this.walletService = walletService;
        this.walletRepository = walletRepository;
    }


    public Card createCard(User user, UUID walletId, CardType cardType, String cardHolderName) {

        Wallet wallet = walletService.fetchWalletById(walletId);

        Card card = Card.builder()
                .owner(user)
                .wallet(wallet)
                .cardType(cardType)
                .cardNumber(generateUniqueCardNumber())
                .cardHolderName(cardHolderName)
                .expiryDate(generateExpiryDate())
                .cvv(cardType == CardType.VIRTUAL ? generateCVV() : null)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        Card savedCard = cardRepository.save(card);
        log.info("Successfully created card with ID [{}] and number [{}] for wallet [{}].", card.getId(), card.getCardNumber(), wallet.getId());

        return savedCard;
    }

    public void createDefaultVirtualCard(User user, UUID walletId) {
        Wallet walletById = walletService.fetchWalletById(walletId);
        createCard(user, walletById.getId(), CardType.VIRTUAL, user.getUsername());
    }

    public Card createPhysicalCard(User user, UUID walletId) {
        // Пример: намираме "default" wallet на потребителя
        Wallet defaultWallet = walletService.fetchWalletById(walletId);
        return createCard(user, defaultWallet.getId(), CardType.PHYSICAL, user.getUsername());
    }



    private String generateUniqueCardNumber() {
        // Генерира 16-цифрен уникален номер за карта
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String generateCVV() {
        // Генерира 3-цифрен CVV код
        return String.format("%03d", (int) (Math.random() * 1000));
    }

    private LocalDateTime generateExpiryDate() {
        // дата на изтичане 3 години напред от текущата дата
        return LocalDateTime.now().plusYears(3);
    }

    public List<Card> getCardsByUser(User user) {
        return cardRepository.findByOwner(user);
    }

}
