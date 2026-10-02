package com.fhict.campaignmanager.mapper;

import com.fhict.campaignmanager.domain.CampaignInvitation;
import com.fhict.campaignmanager.dto.InviteResponse;
import org.springframework.stereotype.Service;

@Service
public class InviteMapper {
    public InviteResponse toInviteResponse(CampaignInvitation campaignInvitation) {
        if (campaignInvitation == null) {
            return null;
        }

        return InviteResponse.builder()
                .id(campaignInvitation.getId())
                .campaign(new CampaignMapper().toCampaignResponse(campaignInvitation.getCampaign()))
                .invitedBy(new UserMapper().toUserResponse(campaignInvitation.getInvitedBy()))
                .invitedUser(new UserMapper().toUserResponse(campaignInvitation.getInvitedUser()))
                .role(campaignInvitation.getRole())
                .invitationStatus(campaignInvitation.getStatus())
                .build();
    }
}
