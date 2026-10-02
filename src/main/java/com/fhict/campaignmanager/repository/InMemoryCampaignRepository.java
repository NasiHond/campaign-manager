package com.fhict.campaignmanager.repository;

import com.fhict.campaignmanager.domain.Campaign;
import com.fhict.campaignmanager.domain.User;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InMemoryCampaignRepository implements CampaignRepository {

    private final Map<Integer, Campaign> campaigns = new HashMap<>();
    private int nextId = 1;

    @Override
    public Campaign save(Campaign campaign) {
        if (campaign.getId() == 0)
        {
            campaign.setId(nextId++);
        }

        campaigns.put(campaign.getId(), campaign);
        return campaign;
    }

    @Override
    public Campaign findById(int id) {
        return campaigns.get(id);
    }

    @Override
    public List<Campaign> findAllByParticipantsContainingKey(User user) {
        return campaigns.values().stream()
                .filter(campaign -> campaign.getParticipants().containsKey(user))
                .toList();
    }

    @Override
    public Campaign update(Campaign campaign) {
        if (campaign.getId() == 0 || !campaigns.containsKey(campaign.getId())) {
            throw new IllegalArgumentException("Campaign does not exist");
        }

        campaigns.put(campaign.getId(), campaign);
        return campaign;
    }

    @Override
    public void delete(int id) {
        campaigns.remove(id);
    }
}
