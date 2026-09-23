package com.fhict.campaignmanager.service;

import com.fhict.campaignmanager.domain.Campaign;
import org.springframework.stereotype.Service;

@Service
public class CampaignService implements ICampaignService
{
    @Override
    public Campaign createCampaign(Campaign campaign) {
        // Implement the logic to create a campaign
        return null;
    }

    @Override
    public Campaign getCampaign(int id) {
        // Implement the logic to retrieve a campaign by ID
        return null;
    }

    @Override
    public Campaign updateCampaign(Campaign campaign) {
        // Implement the logic to update a campaign
        return null;
    }

    @Override
    public void deleteCampaign(int id) {
        // Implement the logic to delete a campaign by ID
    }
}
