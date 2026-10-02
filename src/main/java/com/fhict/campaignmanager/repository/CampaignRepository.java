package com.fhict.campaignmanager.repository;

import com.fhict.campaignmanager.domain.Campaign;
import com.fhict.campaignmanager.domain.User;

import java.util.List;

public interface CampaignRepository {
    Campaign save(Campaign campaign);

    Campaign findById(int id);

    Campaign update(Campaign campaign);

    void delete(int id);

    List<Campaign> findAllByParticipantsContainingKey(User user);
}
