package org.darexapp.card.service;

import lombok.extern.slf4j.Slf4j;
import org.darexapp.card.model.Card;
import org.darexapp.card.model.CardType;
import org.darexapp.card.repository.CardRepository;
import org.darexapp.user.model.User;
import org.darexapp.wallet.model.Wallet;
import org.darexapp.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class CardService {

    private static final String MASTERCARD_PREFIX = "5100";
    private static final int CARD_NUMBER_LENGTH = 16;
    private static final int CVV_LENGTH = 3;
    private static final int CARD_EXPIRY_YEARS = 3;

    private final CardRepository cardRepository;
    private final WalletService walletService;

    @Autowired
    public CardService(
            CardRepository cardRepository,
            WalletService walletService
    ) {
        this.cardRepository = cardRepository;
        this.walletService = walletService;
    }


    @Transactional
    public void createCard(User user, UUID walletId, CardType cardType, String cardHolderName) {
        Wallet wallet = walletService.fetchWalletById(walletId);

        Card card = buildNewCard(user, wallet, cardType, cardHolderName);
        Card savedCard = cardRepository.save(card);

        log.info("Successfully created card with ID [{}] and number [{}] for wallet [{}]",
                savedCard.getId(), savedCard.getCardNumber(), wallet.getId());
    }


    @Transactional
    public void createDefaultVirtualCard(User user, UUID walletId) {
        createCard(user, walletId, CardType.VIRTUAL, user.getUsername());
    }


    @Transactional
    public void createPhysicalCard(User user, UUID walletId) {
        createCard(user, walletId, CardType.PHYSICAL, user.getUsername());
    }


    @Transactional(readOnly = true)
    public List<Card> getCardsByUser(User user) {
        return cardRepository.findByOwner(user);
    }


    private Card buildNewCard(User user, Wallet wallet, CardType cardType, String cardHolderName) {
        LocalDateTime now = LocalDateTime.now();
        return Card.builder()
                .owner(user)
                .wallet(wallet)
                .cardType(cardType)
                .cardNumber(generateMasterCardNumber())
                .cardHolderName(cardHolderName)
                .expiryDate(now.plusYears(CARD_EXPIRY_YEARS))
                .cvv(generateCVV())
                .createdOn(now)
                .updatedOn(now)
                .build();
    }

    private String generateMasterCardNumber() {
        int randomDigitsLength = CARD_NUMBER_LENGTH - MASTERCARD_PREFIX.length() - 1;
        int max = (int) Math.pow(10, randomDigitsLength);
        String randomPart = String.format("%0" + randomDigitsLength + "d", new Random().nextInt(max));
        String partialNumber = MASTERCARD_PREFIX + randomPart;

        int checkDigit = calculateLuhnCheckDigit(partialNumber);
        return partialNumber + checkDigit;
    }

    private int calculateLuhnCheckDigit(String partialNumber) {
        int sum = 0;
        boolean alternate = true;
        
        for (int i = partialNumber.length() - 1; i >= 0; i--) {
            int n = partialNumber.charAt(i) - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        
        return (10 - (sum % 10)) % 10;
    }

    private String generateCVV() {
        return String.format("%0" + CVV_LENGTH + "d", new Random().nextInt(1000));
    }
}
