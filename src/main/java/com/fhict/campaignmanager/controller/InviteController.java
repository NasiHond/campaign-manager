package com.fhict.campaignmanager.controller;

import com.fhict.campaignmanager.domain.CampaignInvite;
import com.fhict.campaignmanager.dto.CreateInviteRequest;
import com.fhict.campaignmanager.dto.InviteResponse;
import com.fhict.campaignmanager.mapper.InviteMapper;
import com.fhict.campaignmanager.service.IInviteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invites")
public class InviteController {

    private final IInviteService inviteService;
    private final InviteMapper inviteMapper;

    public InviteController(IInviteService inviteService, InviteMapper inviteMapper) {
        this.inviteService = inviteService;
        this.inviteMapper = inviteMapper;
    }

    @PostMapping
    public void sendInvite(@RequestBody CreateInviteRequest inviteRequest)
    {
        inviteService.sendInvite(inviteRequest);
    }

    @GetMapping("/{id}")
    public InviteResponse getInvite(@PathVariable int id)
    {
        return inviteMapper.toInviteResponse(inviteService.getInvite(id));
    }

    @GetMapping
    public List<InviteResponse> getAllInvites()
    {
        return toInviteResponses(inviteService.getAllInvitesForUser());
    }

    @GetMapping("/campaigns/{id}")
    public List<InviteResponse> getAllInvitesForCampaign(@PathVariable int id) {
        return toInviteResponses(inviteService.getAllInvitesForCampaign(id));
    }

    private List<InviteResponse> toInviteResponses(List<CampaignInvite> invites) {
        if (invites == null) {
            return null;
        }
        return invites.stream()
                .map(inviteMapper::toInviteResponse)
                .toList();
    }

    @PostMapping("/{id}/accept")
    public void acceptInvite(@PathVariable int id) {
        inviteService.acceptInvite(id);
    }

    @PostMapping("/{id}/decline")
    public void declineInvite(@PathVariable int id) {
        inviteService.declineInvite(id);
    }

    @PostMapping("/{id}/revoke")
    public void revokeInvite(@PathVariable int id) {
        inviteService.revokeInvite(id);
    }
}
