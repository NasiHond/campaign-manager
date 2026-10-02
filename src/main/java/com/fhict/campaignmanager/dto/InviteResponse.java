package com.fhict.campaignmanager.dto;

import com.fhict.campaignmanager.domain.InvitationStatus;
import com.fhict.campaignmanager.domain.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class InviteResponse {
    private int id;
    private CampaignResponse campaign;
    private UserResponse invitedBy;
    private UserResponse invitedUser;
    private Role role;
    private InvitationStatus invitationStatus;
}
