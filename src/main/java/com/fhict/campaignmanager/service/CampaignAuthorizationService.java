package com.fhict.campaignmanager.service;

import com.fhict.campaignmanager.domain.Campaign;
import com.fhict.campaignmanager.domain.Role;
import com.fhict.campaignmanager.domain.User;
import org.springframework.stereotype.Service;

@Service
public class CampaignAuthorizationService {

    public boolean isOwner(Campaign campaign, User user) {
        if (campaign == null || user == null || campaign.getParticipants() == null) {
            return false;
        }

        return campaign.getParticipants().entrySet().stream()
                .anyMatch(entry -> entry.getKey() != null
                        && entry.getKey().getId() == user.getId()
                        && entry.getValue() == Role.OWNER);
    }
}
