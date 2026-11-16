package com.fhict.campaignmanager.service;

import com.fhict.campaignmanager.domain.CampaignInvite;
import com.fhict.campaignmanager.dto.CreateInviteRequest;

import java.util.List;

public interface IInviteService {

    void sendInvite(CreateInviteRequest inviteRequest);

    CampaignInvite getInvite(int inviteId);

    List<CampaignInvite> getAllInvitesForUser();

    List<CampaignInvite> getAllInvitesForCampaign(int campaignId);

    void acceptInvite(int inviteId);

    void declineInvite(int inviteId);

    void revokeInvite(int inviteId);
}
