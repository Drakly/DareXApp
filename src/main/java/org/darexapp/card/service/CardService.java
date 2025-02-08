package org.darexapp.card.service;

import lombok.extern.slf4j.Slf4j;
import org.darexapp.card.model.Card;
import org.darexapp.card.model.CardType;
import org.darexapp.card.repository.CardRepository;
import org.darexapp.user.model.User;
import org.darexapp.wallet.model.Wallet;
import org.darexapp.wallet.model.WalletStatus;
import org.darexapp.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class CardService {

    private final CardRepository cardRepository;
    private final WalletService walletService;

    @Autowired
    public CardService(CardRepository cardRepository, WalletService walletService) {
        this.cardRepository = cardRepository;
        this.walletService = walletService;
    }


    public Card createCard(User user, UUID walletId, CardType cardType, String cardHolderName) {

        Wallet wallet = walletService.getWalletById(walletId);

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
        Wallet walletById = walletService.getWalletById(walletId);
        createCard(user, walletById.getId(), CardType.VIRTUAL, user.getUsername());
    }

//    public Card createPhysicalCard(User user, String cardHolderName) {
//        return createCard(user, null, CardType.PHYSICAL, cardHolderName);
//    }


    private String generateUniqueCardNumber() {
        // Генерира 16-цифрен уникален номер за карта
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String generateCVV() {
        // Генерира 3-цифрен CVV код
        return String.format("%03d", (int) (Math.random() * 1000));
    }

    private LocalDateTime generateExpiryDate() {
        // Задава дата на изтичане 3 години напред от текущата дата
        return LocalDateTime.now().plusYears(3);
    }
}
