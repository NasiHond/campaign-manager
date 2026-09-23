package com.fhict.campaignmanager.service;

import com.fhict.campaignmanager.domain.Campaign;

public interface ICampaignService
{
    Campaign createCampaign(Campaign campaign);

    Campaign getCampaign(int id);

    Campaign updateCampaign(Campaign campaign);

    void deleteCampaign(int id);
}
