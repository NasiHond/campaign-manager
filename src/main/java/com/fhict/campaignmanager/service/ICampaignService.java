package com.fhict.campaignmanager.service;

import com.fhict.campaignmanager.domain.Campaign;
import com.fhict.campaignmanager.dto.CampaignResponse;
import com.fhict.campaignmanager.dto.CreateCampaignRequest;

import java.util.List;

public interface ICampaignService
{
    CampaignResponse createCampaign(CreateCampaignRequest campaign);

    CampaignResponse getCampaign(int id);

    CampaignResponse updateCampaign(CreateCampaignRequest campaign);

    void deleteCampaign(int id);

    List<CampaignResponse> getAllCampaignsFromUser();
}
