package org.darexapp;

import lombok.experimental.UtilityClass;
import org.darexapp.card.model.Card;
import org.darexapp.card.model.CardType;
import org.darexapp.subscription.model.Subscription;
import org.darexapp.subscription.model.SubscriptionPeriod;
import org.darexapp.subscription.model.SubscriptionStatus;
import org.darexapp.subscription.model.SubscriptionType;
import org.darexapp.user.model.Country;
import org.darexapp.user.model.User;
import org.darexapp.user.model.UserRole;
import org.darexapp.wallet.model.Wallet;
import org.darexapp.wallet.model.WalletStatus;
import org.darexapp.wallet.model.WalletType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

@UtilityClass
public class UserTestBuilder {

    public static User customUser() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("User")
                .password("123123")
                .role(UserRole.USER)
                .country(Country.BULGARIA)
                .active(true)
                .registeredAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .owner(user)
                .balance(BigDecimal.ZERO)
                .status(WalletStatus.ACTIVE)
                .type(WalletType.STANDARD)
                .currency(String.valueOf(Currency.getInstance("EUR")))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Subscription subscription = Subscription.builder()
                .id(UUID.randomUUID())
                .type(SubscriptionType.PREMIUM)
                .price(BigDecimal.ZERO)
                .status(SubscriptionStatus.ACTIVE)
                .period(SubscriptionPeriod.MONTHLY)
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now().plusMonths(1))
                .owner(user)
                .active(true)
                .build();


        Card card = Card.builder()
                .id(UUID.randomUUID())
                .owner(user)
                .wallet(wallet)
                .cardType(CardType.VIRTUAL)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();



        user.setSubscriptions(List.of(subscription));
        user.setWallets(List.of(wallet));
        user.setCards(List.of(card));

        return user;

    }
}
