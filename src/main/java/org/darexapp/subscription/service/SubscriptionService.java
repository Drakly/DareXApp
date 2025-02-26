package org.darexapp.subscription.service;

import lombok.extern.slf4j.Slf4j;
import org.darexapp.exception.DomainException;
import org.darexapp.subscription.model.Subscription;
import org.darexapp.subscription.model.SubscriptionPeriod;
import org.darexapp.subscription.model.SubscriptionStatus;
import org.darexapp.subscription.model.SubscriptionType;
import org.darexapp.subscription.repository.SubscriptionRepository;
import org.darexapp.transaction.model.Transaction;
import org.darexapp.transaction.model.TransactionStatus;
import org.darexapp.transaction.service.TransactionService;
import org.darexapp.user.model.User;
import org.darexapp.user.repository.UserRepository;
import org.darexapp.wallet.service.WalletService;
import org.darexapp.web.dto.UpgradeSubscriptionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final WalletService walletService;
    private final TransactionService transactionService;


    @Autowired
    public SubscriptionService(SubscriptionRepository subscriptionRepository, WalletService walletService, TransactionService transactionService) {
        this.subscriptionRepository = subscriptionRepository;
        this.walletService = walletService;
        this.transactionService = transactionService;
    }

    public Subscription createDefaultSubscription(User user) {
        Subscription subscription = subscriptionRepository.save(initializeSubscription(user));
        log.info("New subscription created: ID [{}], Type [{}]", subscription.getId(), subscription.getType());
        return subscription;
    }

    private Subscription initializeSubscription(User user) {
        LocalDateTime now = LocalDateTime.now();

        return Subscription.builder()
                .owner(user)
                .active(true)
                .createdAt(LocalDateTime.now())
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(1))
                .status(SubscriptionStatus.ACTIVE)
                .period(SubscriptionPeriod.MONTHLY)
                .type(SubscriptionType.BASIC)
                .price(BigDecimal.ZERO)
                .active(true)
                .createdAt(now)
                .completedAt(now.plusMonths(1))
                .build();

    }

    @Transactional
    public Transaction upgradeSubscription(User user, SubscriptionType newType, UpgradeSubscriptionRequest request) {

        Optional<Subscription> activeSubscriptionOpt = subscriptionRepository.findByStatusAndOwnerId(SubscriptionStatus.ACTIVE, user.getId());
        if (activeSubscriptionOpt.isEmpty()) {
            throw new DomainException("No active subscription found for user with id [%s]".formatted(user.getId()));
        }
        Subscription currentSub = activeSubscriptionOpt.get();


        SubscriptionPeriod period = request.getSubscription().getPeriod();
        String periodLabel = period.name().substring(0, 1).toUpperCase() + period.name().substring(1).toLowerCase();
        String typeLabel = newType.name().substring(0, 1).toUpperCase() + newType.name().substring(1).toLowerCase();
        String chargeDesc = "Upgrade to %s %s subscription".formatted(periodLabel, typeLabel);


        BigDecimal newPrice = calculateCost(newType, period);


        Transaction chargeTax = walletService.deductFunds(user, request.getWalletId(), newPrice, chargeDesc);
        if (chargeTax.getStatus() == TransactionStatus.FAILED) {
            log.warn("Subscription charge failed for user [{}] with type [{}]", user.getId(), newType);
            return chargeTax;
        }


        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = (period == SubscriptionPeriod.MONTHLY) ? now.plusMonths(1) : now.plusYears(1);

        Subscription upgradedSub = Subscription.builder()
                .owner(user)
                .status(SubscriptionStatus.ACTIVE)
                .period(period)
                .type(newType)
                .price(newPrice)
                .active(period == SubscriptionPeriod.MONTHLY)
                .createdAt(now)
                .completedAt(expiry)
                .build();


        currentSub.setStatus(SubscriptionStatus.COMPLETED);
        currentSub.setCompletedAt(now);


        subscriptionRepository.save(currentSub);
        subscriptionRepository.save(upgradedSub);

        return chargeTax;
    }

    private BigDecimal calculateCost(SubscriptionType type, SubscriptionPeriod period) {

        if (type == SubscriptionType.BASIC) {
            return BigDecimal.ZERO;
        } else if (type == SubscriptionType.PREMIUM && period == SubscriptionPeriod.MONTHLY) {
            return new BigDecimal("15.99");
        } else if (type == SubscriptionType.PREMIUM && period == SubscriptionPeriod.YEARLY) {
            return new BigDecimal("159.99");
        } else if (type == SubscriptionType.METAL && period == SubscriptionPeriod.MONTHLY) {
            return new BigDecimal("89.99");
        } else  {
            return new BigDecimal("300.99");
        }
    }

    public List<Subscription> fetchSubscriptionsDueForRenewal() {
        return subscriptionRepository.findAllByStatusAndCompletedAtLessThanEqual(SubscriptionStatus.ACTIVE, LocalDateTime.now());
    }

    public void completeSubscription(Subscription subscription) {
        subscription.setStatus(SubscriptionStatus.COMPLETED);
        subscription.setCompletedAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);
    }

    public void terminateSubscription(Subscription subscription) {
        subscription.setStatus(SubscriptionStatus.TERMINATED);
        subscription.setCompletedAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);
    }


}

