package com.fhict.campaignmanager.service;

import com.fhict.campaignmanager.dto.CampaignResponse;
import com.fhict.campaignmanager.dto.CreateCampaignRequest;
import com.fhict.campaignmanager.dto.UpdateCampaignRequest;

import java.util.List;

public interface ICampaignService
{
    CampaignResponse createCampaign(CreateCampaignRequest campaign);

    CampaignResponse getCampaign(int id);

    CampaignResponse updateCampaign(int id, UpdateCampaignRequest updateRequest);

    CampaignResponse updateParticipantRole(int campaignId, int userId, String role);

    void removeParticipant(int campaignId, int userId);

    void deleteCampaign(int id);

    List<CampaignResponse> getAllCampaignsFromUser();
}
