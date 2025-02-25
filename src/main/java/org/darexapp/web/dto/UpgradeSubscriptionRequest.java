package org.darexapp.web.dto;

import lombok.Builder;
import lombok.Data;
import org.darexapp.subscription.model.Subscription;

import java.util.UUID;

@Data
@Builder
public class UpgradeSubscriptionRequest {

    private Subscription subscription;

    private UUID walletId;
}
