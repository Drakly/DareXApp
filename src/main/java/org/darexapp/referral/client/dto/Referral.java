package org.darexapp.referral.client.dto;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class Referral {

    private UUID userId;

    private String referralCode;

    private LocalDateTime createdAt;

    private int clickCount = 0;
}
