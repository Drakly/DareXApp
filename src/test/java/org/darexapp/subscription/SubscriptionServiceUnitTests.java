package org.darexapp.subscription;

import org.darexapp.subscription.model.Subscription;
import org.darexapp.subscription.model.SubscriptionPeriod;
import org.darexapp.subscription.model.SubscriptionStatus;
import org.darexapp.subscription.model.SubscriptionType;
import org.darexapp.subscription.repository.SubscriptionRepository;
import org.darexapp.subscription.service.SubscriptionService;
import org.darexapp.transaction.model.Transaction;
import org.darexapp.transaction.model.TransactionStatus;
import org.darexapp.user.model.User;
import org.darexapp.wallet.service.WalletService;
import org.darexapp.web.dto.UpgradeSubscriptionRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceUnitTests {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Test
    void testCreateDefaultSubscription_success() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");

        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> {
            Subscription sub = invocation.getArgument(0);
            sub.setId(UUID.randomUUID());
            return sub;
        });

        Subscription subscription = subscriptionService.createDefaultSubscription(user);

        assertNotNull(subscription.getId());
        assertEquals(user, subscription.getOwner());
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        assertEquals(SubscriptionPeriod.MONTHLY, subscription.getPeriod());
        assertEquals(SubscriptionType.BASIC, subscription.getType());
        assertEquals(BigDecimal.ZERO, subscription.getPrice());
        assertTrue(subscription.getCompletedAt().isAfter(subscription.getCreatedAt()));

        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void testUpgradeSubscription_success() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");

        Subscription currentSub = Subscription.builder()
                .id(UUID.randomUUID())
                .owner(user)
                .status(SubscriptionStatus.ACTIVE)
                .period(SubscriptionPeriod.MONTHLY)
                .type(SubscriptionType.BASIC)
                .price(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now().minusDays(10))
                .completedAt(LocalDateTime.now().plusDays(20))
                .build();
        when(subscriptionRepository.findByStatusAndOwnerId(SubscriptionStatus.ACTIVE, user.getId()))
                .thenReturn(Optional.of(currentSub));

        UpgradeSubscriptionRequest request = UpgradeSubscriptionRequest.builder().build();
        UUID walletId = UUID.randomUUID();
        request.setWalletId(walletId);
        request.setSubscriptionPeriod(SubscriptionPeriod.MONTHLY);

        SubscriptionType newType = SubscriptionType.PREMIUM;
        BigDecimal expectedPrice = new BigDecimal("15.99");
        Transaction successfulTx = new Transaction();
        successfulTx.setStatus(TransactionStatus.SUCCESSFUL);
        when(walletService.deductFunds(eq(user), eq(walletId), eq(expectedPrice), anyString()))
                .thenReturn(successfulTx);

        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = subscriptionService.upgradeSubscription(user, newType, request);

        assertEquals(TransactionStatus.SUCCESSFUL, result.getStatus());
        assertEquals(SubscriptionStatus.COMPLETED, currentSub.getStatus());
        verify(subscriptionRepository, times(2)).save(any(Subscription.class));
    }

    @Test
    void testUpgradeSubscription_failedCharge() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");

        Subscription currentSub = Subscription.builder()
                .id(UUID.randomUUID())
                .owner(user)
                .status(SubscriptionStatus.ACTIVE)
                .period(SubscriptionPeriod.MONTHLY)
                .type(SubscriptionType.BASIC)
                .price(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now().minusDays(10))
                .completedAt(LocalDateTime.now().plusDays(20))
                .build();
        when(subscriptionRepository.findByStatusAndOwnerId(SubscriptionStatus.ACTIVE, user.getId()))
                .thenReturn(Optional.of(currentSub));

        UpgradeSubscriptionRequest request = UpgradeSubscriptionRequest.builder().build();
        UUID walletId = UUID.randomUUID();
        request.setWalletId(walletId);
        request.setSubscriptionPeriod(SubscriptionPeriod.MONTHLY);

        SubscriptionType newType = SubscriptionType.GOLD;
        BigDecimal expectedPrice = new BigDecimal("19.99");

        Transaction failedTx = new Transaction();
        failedTx.setStatus(TransactionStatus.FAILED);
        when(walletService.deductFunds(eq(user), eq(walletId), eq(expectedPrice), anyString()))
                .thenReturn(failedTx);

        Transaction result = subscriptionService.upgradeSubscription(user, newType, request);

        assertEquals(TransactionStatus.FAILED, result.getStatus());
        verify(subscriptionRepository, never()).save(argThat(sub -> sub.getStatus() == SubscriptionStatus.COMPLETED));
    }

    @Test
    void testFetchSubscriptionsDueForRenewal() {
        Subscription sub1 = Subscription.builder().id(UUID.randomUUID()).build();
        Subscription sub2 = Subscription.builder().id(UUID.randomUUID()).build();
        List<Subscription> subscriptions = List.of(sub1, sub2);
        when(subscriptionRepository.findAllByStatusAndCompletedAtLessThanEqual(eq(SubscriptionStatus.ACTIVE), any(LocalDateTime.class)))
                .thenReturn(subscriptions);

        List<Subscription> result = subscriptionService.fetchSubscriptionsDueForRenewal();

        assertEquals(2, result.size());
        verify(subscriptionRepository).findAllByStatusAndCompletedAtLessThanEqual(eq(SubscriptionStatus.ACTIVE), any(LocalDateTime.class));
    }

    @Test
    void testCompleteSubscription() {
        Subscription sub = Subscription.builder()
                .id(UUID.randomUUID())
                .status(SubscriptionStatus.ACTIVE)
                .completedAt(LocalDateTime.now().plusDays(10))
                .build();

        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        subscriptionService.completeSubscription(sub);
        assertEquals(SubscriptionStatus.COMPLETED, sub.getStatus());
        assertNotNull(sub.getCompletedAt());
        verify(subscriptionRepository).save(sub);
    }

    @Test
    void testTerminateSubscription() {
        Subscription sub = Subscription.builder()
                .id(UUID.randomUUID())
                .status(SubscriptionStatus.ACTIVE)
                .completedAt(LocalDateTime.now().plusDays(10))
                .build();

        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        subscriptionService.terminateSubscription(sub);

        assertEquals(SubscriptionStatus.TERMINATED, sub.getStatus());
        assertNotNull(sub.getCompletedAt());
        verify(subscriptionRepository).save(sub);
    }
}
