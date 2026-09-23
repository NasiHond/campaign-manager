package com.fhict.campaignmanager.repository;

import com.fhict.campaignmanager.domain.Campaign;

public interface CampaignRepository {
    Campaign save(Campaign campaign);

    Campaign findById(int id);

    Campaign update(Campaign campaign);

    void delete(int id);
}
