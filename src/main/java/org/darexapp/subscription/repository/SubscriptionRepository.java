package org.darexapp.subscription.repository;

import org.darexapp.subscription.model.Subscription;
import org.darexapp.subscription.model.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByStatusAndOwnerId(SubscriptionStatus subscriptionStatus, UUID id);

    List<Subscription> findAllByStatusAndCompletedAtLessThanEqual(SubscriptionStatus subscriptionStatus, LocalDateTime now);
}
