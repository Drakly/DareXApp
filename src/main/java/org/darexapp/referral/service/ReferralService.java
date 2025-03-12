package org.darexapp.referral.service;

import lombok.extern.slf4j.Slf4j;
import org.darexapp.referral.client.ReferralClient;
import org.darexapp.referral.client.dto.ReferralRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class ReferralService {

    private final ReferralClient referralClient;

    @Autowired
    public ReferralService(ReferralClient referralClient) {
        this.referralClient = referralClient;
    }

    public void createReferral(ReferralRequest referralRequest) {
        ReferralRequest newReferral = ReferralRequest.builder()
                .userId(referralRequest.getUserId())
                .referralCode(referralRequest.getReferralCode())
                .createdAt(LocalDateTime.now())
                .clickCount(0)
                .build();

        ResponseEntity<ReferralRequest> response = referralClient.createReferral(newReferral);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            ReferralRequest created = response.getBody();
            log.info("Successfully created referral with id {} and code {}",
                    created.getId(), created.getReferralCode());
        } else {
            log.error("Failed to create referral for user {}", referralRequest.getUserId());
            throw new RuntimeException("Failed to create referral for user " + referralRequest.getUserId());
        }
    }


    public ReferralRequest getReferral(UUID userId) {
        ResponseEntity<ReferralRequest> httpResponse = referralClient.getReferralByUser(userId);
        if (httpResponse.getStatusCode().is2xxSuccessful()) {
            ReferralRequest referral = httpResponse.getBody();
            if (referral == null) {
                return ReferralRequest.builder()
                        .userId(userId)
                        .referralCode("")
                        .clickCount(0)
                        .build();
            }
            return referral;
        } else {
            log.warn("Referral not found for user {}", userId);
            return ReferralRequest.builder()
                    .userId(userId)
                    .referralCode("")
                    .clickCount(0)
                    .build();
        }
    }

    public void incrementClickCount(String referralCode) {
        ResponseEntity<Void> httpResponse = referralClient.trackReferral(referralCode);

        if (!httpResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to track referral with id " + referralCode);
        }

    }
}
