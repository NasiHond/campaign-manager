package com.fhict.campaignmanager.mapper;

import com.fhict.campaignmanager.domain.CampaignInvite;
import com.fhict.campaignmanager.dto.InviteResponse;
import org.springframework.stereotype.Service;

@Service
public class InviteMapper {
    public InviteResponse toInviteResponse(CampaignInvite campaignInvite) {
        if (campaignInvite == null) {
            return null;
        }

        return InviteResponse.builder()
                .id(campaignInvite.getId())
                .campaign(new CampaignMapper().toCampaignResponse(campaignInvite.getCampaign()))
                .invitedBy(new UserMapper().toUserResponse(campaignInvite.getInvitedBy()))
                .invitedUser(new UserMapper().toUserResponse(campaignInvite.getInvitedUser()))
                .role(campaignInvite.getRole())
                .inviteStatus(campaignInvite.getStatus())
                .expiresAt(campaignInvite.getExpiresAt())
                .build();
    }
}
