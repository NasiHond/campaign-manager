package com.fhict.campaignmanager.repository;

import com.fhict.campaignmanager.domain.CampaignInvitation;

import java.util.List;

public interface InviteRepository {
    CampaignInvitation save(CampaignInvitation invitation);

    CampaignInvitation findById(int id);

    List<CampaignInvitation> findAllByUserId(int userId);

    List<CampaignInvitation> findAllByCampaignId(int campaignId);
}
