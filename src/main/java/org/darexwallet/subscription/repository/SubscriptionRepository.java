package org.darexwallet.subscription.repository;

import org.darexwallet.subscription.model.Subscription;
import org.darexwallet.subscription.model.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByStatusAndOwnerId(SubscriptionStatus status, UUID ownerId);

    List<Subscription> findByOwnerIdOrderByCreatedOnDesc(UUID ownerId);

    List<Subscription> findAllByStatusAndCompletedOnBefore(SubscriptionStatus status, LocalDateTime completedOn);
}
