package org.darexapp.card;

import org.darexapp.card.model.Card;
import org.darexapp.card.model.CardType;
import org.darexapp.card.repository.CardRepository;
import org.darexapp.card.service.CardService;
import org.darexapp.user.model.User;
import org.darexapp.wallet.model.Wallet;
import org.darexapp.wallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CardServiceUnitTests {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private WalletService walletService;

    @InjectMocks
    private CardService cardService;

    @Test
    void testCreateCard() {
        User user = new User();
        user.setUsername("John");
        Wallet wallet = new Wallet();
        UUID walletId = UUID.randomUUID();
        wallet.setId(walletId);
        when(walletService.fetchWalletById(walletId)).thenReturn(wallet);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card c = invocation.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        cardService.createCard(user, walletId, CardType.VIRTUAL, user.getUsername());

        ArgumentCaptor<Card> cap = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(cap.capture());
        Card saved = cap.getValue();
        assertNotNull(saved.getId());
        assertEquals(user, saved.getOwner());
        assertEquals(wallet, saved.getWallet());
        assertEquals(CardType.VIRTUAL, saved.getCardType());
        assertEquals("John", saved.getCardHolderName());
    }

    @Test
    void testCreateDefaultVirtualCard() {
        User user = new User();
        user.setUsername("Alice");
        Wallet wallet = new Wallet();
        UUID walletId = UUID.randomUUID();
        wallet.setId(walletId);
        when(walletService.fetchWalletById(walletId)).thenReturn(wallet);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card c = invocation.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        cardService.createDefaultVirtualCard(user, walletId);

        ArgumentCaptor<Card> cap = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(cap.capture());
        assertEquals(CardType.VIRTUAL, cap.getValue().getCardType());
        assertEquals("Alice", cap.getValue().getCardHolderName());
    }

    @Test
    void testCreatePhysicalCard() {
        User user = new User();
        user.setUsername("Bob");
        Wallet wallet = new Wallet();
        UUID walletId = UUID.randomUUID();
        wallet.setId(walletId);
        when(walletService.fetchWalletById(walletId)).thenReturn(wallet);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card c = invocation.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        cardService.createPhysicalCard(user, walletId);

        ArgumentCaptor<Card> cap = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(cap.capture());
        assertEquals(CardType.PHYSICAL, cap.getValue().getCardType());
    }

    @Test
    void testGetCardsByUser() {
        User user = new User();
        user.setUsername("TestUser");
        Card card1 = Card.builder().id(UUID.randomUUID()).build();
        Card card2 = Card.builder().id(UUID.randomUUID()).build();
        when(cardRepository.findByOwner(user)).thenReturn(List.of(card1, card2));

        List<Card> cards = cardService.getCardsByUser(user);

        assertEquals(2, cards.size());
        assertTrue(cards.contains(card1));
        assertTrue(cards.contains(card2));
    }

    @Test
    void testCardNumberAndCVVFormat() {
        User user = new User();
        user.setUsername("Charlie");
        Wallet wallet = new Wallet();
        UUID walletId = UUID.randomUUID();
        wallet.setId(walletId);
        when(walletService.fetchWalletById(walletId)).thenReturn(wallet);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card c = invocation.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        cardService.createCard(user, walletId, CardType.VIRTUAL, "Charlie");

        ArgumentCaptor<Card> cap = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(cap.capture());
        Card saved = cap.getValue();
        assertNotNull(saved.getCardNumber());
        assertEquals(16, saved.getCardNumber().length());
        assertTrue(saved.getCardNumber().startsWith("5100"));
        assertNotNull(saved.getCvv());
        assertEquals(3, saved.getCvv().length());
        assertTrue(saved.getCvv().matches("\\d{3}"));
    }

}
