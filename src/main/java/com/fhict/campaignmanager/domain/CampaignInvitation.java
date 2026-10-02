package com.fhict.campaignmanager.domain;

import lombok.*;

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
}

