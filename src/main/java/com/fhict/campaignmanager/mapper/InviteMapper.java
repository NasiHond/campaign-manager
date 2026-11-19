package com.fhict.campaignmanager.mapper;

import com.fhict.campaignmanager.domain.CampaignInvite;
import com.fhict.campaignmanager.dto.InviteResponse;
import org.springframework.stereotype.Service;

@Service
public class InviteMapper {
    private final CampaignMapper campaignMapper;
    private final UserMapper userMapper;

    public InviteMapper(CampaignMapper campaignMapper, UserMapper userMapper) {
        this.campaignMapper = campaignMapper;
        this.userMapper = userMapper;
    }

    public InviteResponse toInviteResponse(CampaignInvite campaignInvite) {
        if (campaignInvite == null) {
            return null;
        }

        return InviteResponse.builder()
                .id(campaignInvite.getId())
                .campaign(campaignMapper.toCampaignResponse(campaignInvite.getCampaign()))
                .invitedBy(userMapper.toUserResponse(campaignInvite.getInvitedBy()))
                .invitedUser(userMapper.toUserResponse(campaignInvite.getInvitedUser()))
                .role(campaignInvite.getRole())
                .inviteStatus(campaignInvite.getStatus())
                .expiresAt(campaignInvite.getExpiresAt())
                .build();
    }
}
