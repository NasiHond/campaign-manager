package com.fhict.campaignmanager.mapper;

import com.fhict.campaignmanager.domain.Campaign;
import com.fhict.campaignmanager.dto.CampaignResponse;
import org.springframework.stereotype.Service;

@Service
public class CampaignMapper {
    public CampaignResponse toCampaignResponse(Campaign campaign) {
        if (campaign == null) {
            return null;
        }

        return CampaignResponse.builder()
                .id(campaign.getId())
                .name(campaign.getName())
                .description(campaign.getDescription())
                .participants(campaign.getParticipants())
                .ownerId(campaign.getParticipants() == null
                        ? null
                        : campaign.getParticipants().entrySet().stream()
                        .filter(entry -> entry.getValue() == com.fhict.campaignmanager.domain.Role.OWNER)
                        .map(entry -> entry.getKey().getId())
                        .findFirst()
                        .orElse(null))
                .build();
    }
}
