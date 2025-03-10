package org.darexapp.referral.client.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ReferralRequest {

    private UUID id;

    private UUID userId;

    private String referralCode;

    private LocalDateTime createdAt;

    private int clickCount;
}
