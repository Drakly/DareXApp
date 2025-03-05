package org.darexapp.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.darexapp.subscription.model.Subscription;
import org.darexapp.subscription.service.SubscriptionService;
import org.darexapp.transaction.model.Transaction;
import org.darexapp.transaction.model.TransactionStatus;
import org.darexapp.user.model.User;
import org.darexapp.web.dto.UpgradeSubscriptionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RenewalSubscriptionProcessor {

    private final SubscriptionService subscriptionService;

    @Autowired
    public RenewalSubscriptionProcessor(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Scheduled(fixedDelay = 200000)
    public void processRenewals() {
        List<Subscription> subscriptions = subscriptionService.fetchSubscriptionsDueForRenewal();

        if (subscriptions.isEmpty()) {
            log.info("No subscriptions for renewal was found.");
            return;
        }

        Map<Boolean, List<Subscription>> groupedSubs = subscriptions.stream()
                .collect(Collectors.partitioningBy(Subscription::isActive));

        List<Subscription> nonRenewable = groupedSubs.getOrDefault(false, new ArrayList<>());
        for (Subscription sub : nonRenewable) {
            subscriptionService.completeSubscription(sub);
            subscriptionService.createDefaultSubscription(sub.getOwner());
        }

        List<Subscription> renewable = groupedSubs.getOrDefault(true, new ArrayList<>());
        for (Subscription sub : renewable) {
            processRenewableSubscription(sub);
        }
    }

    private void processRenewableSubscription(Subscription subscription) {
        User owner = subscription.getOwner();
        UUID walletId = owner.getWallets().get(0).getId();

        UpgradeSubscriptionRequest upgradeRequest = UpgradeSubscriptionRequest.builder()
                .subscriptionPeriod(subscription.getPeriod())
                .walletId(walletId)
                .build();

        Transaction transaction = subscriptionService.upgradeSubscription(owner, subscription.getType(), upgradeRequest);
        if (transaction.getStatus() == TransactionStatus.FAILED) {
            subscriptionService.completeSubscription(subscription);
            subscriptionService.createDefaultSubscription(owner);
        }
    }
}
