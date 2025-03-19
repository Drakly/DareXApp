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
import org.darexapp.wallet.service.WalletService;
import org.darexapp.web.dto.UpgradeSubscriptionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class SubscriptionService {

    private static final BigDecimal PREMIUM_MONTHLY_PRICE = new BigDecimal("15.99");
    private static final BigDecimal PREMIUM_YEARLY_PRICE = new BigDecimal("159.99");
    private static final BigDecimal GOLD_MONTHLY_PRICE = new BigDecimal("19.99");
    private static final BigDecimal GOLD_YEARLY_PRICE = new BigDecimal("199.99");
    private static final BigDecimal METAL_MONTHLY_PRICE = new BigDecimal("89.99");
    private static final BigDecimal METAL_YEARLY_PRICE = new BigDecimal("300.99");

    private final SubscriptionRepository subscriptionRepository;
    private final WalletService walletService;


    @Autowired
    public SubscriptionService(
            SubscriptionRepository subscriptionRepository,
            WalletService walletService
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.walletService = walletService;
    }


    @Transactional
    public Subscription createDefaultSubscription(User user) {
        Subscription subscription = subscriptionRepository.save(initializeSubscription(user));
        log.info("New subscription created: ID [{}], Type [{}]", subscription.getId(), subscription.getType());
        return subscription;
    }


    @Transactional
    public Transaction upgradeSubscription(User user, SubscriptionType newType, UpgradeSubscriptionRequest request) {
        Subscription currentSub = getCurrentActiveSubscription(user);
        
        String chargeDesc = formatUpgradeDescription(request.getSubscriptionPeriod(), newType);
        BigDecimal newPrice = calculateCost(newType, request.getSubscriptionPeriod());

        Transaction chargeTax = processUpgradeCharge(user, request, newPrice, chargeDesc);
        if (chargeTax.getStatus() == TransactionStatus.FAILED) {
            log.warn("Subscription charge failed for user [{}] with type [{}]", user.getId(), newType);
            return chargeTax;
        }

        updateSubscriptions(currentSub, user, newType, request.getSubscriptionPeriod(), newPrice);
        
        return chargeTax;
    }


    @Transactional(readOnly = true)
    public List<Subscription> fetchSubscriptionsDueForRenewal() {
        return subscriptionRepository.findAllByStatusAndCompletedAtLessThanEqual(
                SubscriptionStatus.ACTIVE,
                LocalDateTime.now()
        );
    }


    @Transactional
    public void completeSubscription(Subscription subscription) {
        updateSubscriptionStatus(subscription, SubscriptionStatus.COMPLETED);
    }


    @Transactional
    public void terminateSubscription(Subscription subscription) {
        updateSubscriptionStatus(subscription, SubscriptionStatus.TERMINATED);
    }



    private Subscription initializeSubscription(User user) {
        LocalDateTime now = LocalDateTime.now();
        return Subscription.builder()
                .owner(user)
                .active(true)
                .createdAt(now)
                .status(SubscriptionStatus.ACTIVE)
                .period(SubscriptionPeriod.MONTHLY)
                .type(SubscriptionType.BASIC)
                .price(BigDecimal.ZERO)
                .completedAt(now.plusMonths(1))
                .build();
    }

    private Subscription getCurrentActiveSubscription(User user) {
        return subscriptionRepository.findByStatusAndOwnerId(SubscriptionStatus.ACTIVE, user.getId())
                .orElseThrow(() -> new DomainException(
                        "No active subscription found for user with id [%s]".formatted(user.getId())
                ));
    }

    private String formatUpgradeDescription(SubscriptionPeriod period, SubscriptionType type) {
        String periodLabel = capitalizeFirstLetter(period.name());
        String typeLabel = capitalizeFirstLetter(type.name());
        return "Upgrade to %s %s subscription".formatted(periodLabel, typeLabel);
    }

    private String capitalizeFirstLetter(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    private Transaction processUpgradeCharge(
            User user,
            UpgradeSubscriptionRequest request,
            BigDecimal amount,
            String description
    ) {
        return walletService.deductFunds(user, request.getWalletId(), amount, description);
    }

    private void updateSubscriptions(
            Subscription currentSub,
            User user,
            SubscriptionType newType,
            SubscriptionPeriod period,
            BigDecimal price
    ) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = calculateExpiryDate(now, period);

        Subscription upgradedSub = buildUpgradedSubscription(user, newType, period, price, now, expiry);
        
        currentSub.setStatus(SubscriptionStatus.COMPLETED);
        currentSub.setCompletedAt(now);

        subscriptionRepository.save(currentSub);
        subscriptionRepository.save(upgradedSub);
    }

    private LocalDateTime calculateExpiryDate(LocalDateTime startDate, SubscriptionPeriod period) {
        return period == SubscriptionPeriod.MONTHLY ? 
                startDate.plusMonths(1) : startDate.plusYears(1);
    }

    private Subscription buildUpgradedSubscription(
            User user,
            SubscriptionType type,
            SubscriptionPeriod period,
            BigDecimal price,
            LocalDateTime createdAt,
            LocalDateTime completedAt
    ) {
        return Subscription.builder()
                .owner(user)
                .status(SubscriptionStatus.ACTIVE)
                .period(period)
                .type(type)
                .price(price)
                .active(period == SubscriptionPeriod.MONTHLY)
                .createdAt(createdAt)
                .completedAt(completedAt)
                .build();
    }

    private void updateSubscriptionStatus(Subscription subscription, SubscriptionStatus status) {
        subscription.setStatus(status);
        subscription.setCompletedAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);
        log.info("Updated subscription [{}] status to [{}]", subscription.getId(), status);
    }

    private BigDecimal calculateCost(SubscriptionType type, SubscriptionPeriod period) {
        if (type == SubscriptionType.BASIC) {
            return BigDecimal.ZERO;
        }

        return switch (type) {
            case PREMIUM -> period == SubscriptionPeriod.MONTHLY ? PREMIUM_MONTHLY_PRICE : PREMIUM_YEARLY_PRICE;
            case GOLD -> period == SubscriptionPeriod.MONTHLY ? GOLD_MONTHLY_PRICE : GOLD_YEARLY_PRICE;
            case METAL -> period == SubscriptionPeriod.MONTHLY ? METAL_MONTHLY_PRICE : METAL_YEARLY_PRICE;
            default -> throw new IllegalArgumentException("Unsupported subscription type: " + type);
        };
    }
}

