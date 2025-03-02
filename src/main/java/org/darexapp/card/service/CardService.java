package org.darexapp.card.service;

import lombok.extern.slf4j.Slf4j;
import org.darexapp.card.model.Card;
import org.darexapp.card.model.CardType;
import org.darexapp.card.repository.CardRepository;
import org.darexapp.user.model.User;
import org.darexapp.wallet.model.Wallet;
import org.darexapp.wallet.repository.WalletRepository;
import org.darexapp.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
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


    public void createCard(User user, UUID walletId, CardType cardType, String cardHolderName) {

        Wallet wallet = walletService.fetchWalletById(walletId);

        Card card = Card.builder()
                .owner(user)
                .wallet(wallet)
                .cardType(cardType)
                .cardNumber(generateMasterCardNumber())
                .cardHolderName(cardHolderName)
                .expiryDate(generateExpiryDate())
                .cvv(generateCVV())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        Card savedCard = cardRepository.save(card);
        log.info("Successfully created card with ID [{}] and number [{}] for wallet [{}].", card.getId(), card.getCardNumber(), wallet.getId());

    }

    public void createDefaultVirtualCard(User user, UUID walletId) {
        Wallet walletById = walletService.fetchWalletById(walletId);
        createCard(user, walletById.getId(), CardType.VIRTUAL, user.getUsername());
    }

    public void createPhysicalCard(User user, UUID walletId) {
        Wallet defaultWallet = walletService.fetchWalletById(walletId);
        createCard(user, defaultWallet.getId(), CardType.PHYSICAL, user.getUsername());
    }

    private String generateMasterCardNumber() {
        String prefix = "5100"; // Префикс за MasterCard
        int randomDigitsLength = 16 - prefix.length() - 1; // 16-цифрен номер: 4 (prefix) + 11 (random) + 1 (check)
        int max = (int) Math.pow(10, randomDigitsLength);
        // Генерираме произволната част с водещи нули, ако е необходимо
        String randomPart = String.format("%0" + randomDigitsLength + "d", new Random().nextInt(max));
        String partialNumber = prefix + randomPart;

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
        int checkDigit = (10 - (sum % 10)) % 10;

        return partialNumber + checkDigit;
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
