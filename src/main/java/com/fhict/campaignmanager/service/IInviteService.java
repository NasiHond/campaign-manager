package com.fhict.campaignmanager.service;

import com.fhict.campaignmanager.dto.InviteResponse;

import java.util.List;

public interface IInviteService {

    void sendInvite(int campaignId, String identifier);

    InviteResponse getInvite(int inviteId);

    List<InviteResponse> getAllInvitesForUser();

    List<InviteResponse> getAllInvitesForCampaign(int campaignId);
}
