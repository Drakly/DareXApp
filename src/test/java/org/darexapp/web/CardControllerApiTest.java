package org.darexapp.web;

import org.darexapp.card.model.Card;
import org.darexapp.card.service.CardService;
import org.darexapp.security.CustomUserDetails;
import org.darexapp.subscription.model.Subscription;
import org.darexapp.subscription.model.SubscriptionPeriod;
import org.darexapp.subscription.model.SubscriptionStatus;
import org.darexapp.subscription.model.SubscriptionType;
import org.darexapp.subscription.service.SubscriptionService;
import org.darexapp.user.model.User;
import org.darexapp.user.model.UserRole;
import org.darexapp.user.service.UserService;
import org.darexapp.wallet.model.Wallet;
import org.darexapp.web.controller.CardController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
public class CardControllerApiTest {

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private SubscriptionService subscriptionService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getCards_withAnyAuthenticatedUser_shouldReturnCardsView() throws Exception {

        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setUsername("user1");

        when(userService.findById(userId)).thenReturn(user);

        Subscription subscription = new Subscription();
        subscription.setType(SubscriptionType.PREMIUM);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setPeriod(SubscriptionPeriod.MONTHLY);
        user.setSubscriptions(Collections.singletonList(subscription));

        CustomUserDetails principal = new CustomUserDetails(
                userId, "User123", "Kirilov06@", UserRole.USER, true);

        List<Card> cards = Collections.emptyList();
        when(cardService.getCardsByUser(user)).thenReturn(cards);

        MockHttpServletRequestBuilder request = get("/cards")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("cards"))
                .andExpect(model().attribute("currentUser", user))
                .andExpect(model().attribute("cards", cards))
                .andExpect(model().attribute("currentUser",
                        org.hamcrest.Matchers.hasProperty("subscriptions",
                                org.hamcrest.Matchers.hasItem(
                                        org.hamcrest.Matchers.hasProperty("type", org.hamcrest.Matchers.hasToString("PREMIUM"))
                                )
                        )
                ));



    }

    @Test
    void postPhysicalCard_withAuthenticatedUser_shouldRedirectToCards() throws Exception {
        UUID userId = UUID.randomUUID();
        CustomUserDetails customUser = new CustomUserDetails(userId, "user1", "password", UserRole.USER, true);

        User user = new User();
        user.setId(userId);
        user.setUsername("user1");

        Wallet wallet = new Wallet();
        UUID walletId = UUID.randomUUID();
        wallet.setId(walletId);
        user.setWallets(List.of(wallet));

        when(userService.findById(userId)).thenReturn(user);

        mockMvc.perform(post("/cards/physical")
                        .with(user(customUser))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cards"));

        verify(cardService, times(1)).createPhysicalCard(user, walletId);
    }
}
