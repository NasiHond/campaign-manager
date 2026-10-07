package com.fhict.campaignmanager.service;

import com.fhict.campaignmanager.dto.CreateInviteRequest;
import com.fhict.campaignmanager.dto.InviteResponse;

import java.util.List;

public interface IInviteService {

    void sendInvite(CreateInviteRequest inviteRequest);

    InviteResponse getInvite(int inviteId);

    List<InviteResponse> getAllInvitesForUser();

    List<InviteResponse> getAllInvitesForCampaign(int campaignId);

    void acceptInvite(int inviteId);

    void declineInvite(int inviteId);

    void revokeInvite(int inviteId);
}
