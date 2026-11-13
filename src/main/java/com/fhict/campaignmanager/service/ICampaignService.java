package com.fhict.campaignmanager.service;

import com.fhict.campaignmanager.domain.Campaign;
import com.fhict.campaignmanager.dto.CreateCampaignRequest;
import com.fhict.campaignmanager.dto.UpdateCampaignRequest;

import java.util.List;

public interface ICampaignService
{
    Campaign createCampaign(CreateCampaignRequest campaign);

    Campaign getCampaign(int id);

    Campaign updateCampaign(int id, UpdateCampaignRequest updateRequest);

    Campaign updateParticipantRole(int campaignId, int userId, String role);

    void removeParticipant(int campaignId, int userId);

    void deleteCampaign(int id);

    List<Campaign> getAllCampaignsFromUser();
}
