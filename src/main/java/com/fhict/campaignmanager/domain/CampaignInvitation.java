package com.fhict.campaignmanager.domain;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CampaignInvitation {
    private int id;
    private Campaign campaign;
    private User invitedBy;
    private User invitedUser;
    private Role role;
    private InvitationStatus status;
    private Instant expiresAt;
}
