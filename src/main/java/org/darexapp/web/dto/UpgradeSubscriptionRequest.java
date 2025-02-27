package org.darexapp.web.dto;

import lombok.Builder;
import lombok.Data;
import org.darexapp.subscription.model.SubscriptionPeriod;


import java.util.UUID;

@Data
@Builder
public class UpgradeSubscriptionRequest {

    private SubscriptionPeriod subscriptionPeriod;

    private UUID walletId;
}
