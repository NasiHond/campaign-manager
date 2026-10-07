package com.fhict.campaignmanager.controller;

import com.fhict.campaignmanager.dto.CreateInviteRequest;
import com.fhict.campaignmanager.dto.InviteResponse;
import com.fhict.campaignmanager.service.IInviteService;
import com.fhict.campaignmanager.service.InviteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invites")
public class InviteController {

    private final IInviteService inviteService;

    public InviteController(IInviteService inviteService) {
        this.inviteService = inviteService;
    }

    @CrossOrigin
    @PostMapping
    public void sendInvite(@RequestBody CreateInviteRequest inviteRequest) {
        inviteService.sendInvite(inviteRequest);
    }

    @CrossOrigin
    @GetMapping("/{id}")
    public InviteResponse getInvite(@PathVariable int id) {
        return inviteService.getInvite(id);
    }

    @CrossOrigin
    @GetMapping
    public List<InviteResponse> getAllInvites() {
        return inviteService.getAllInvitesForUser();
    }

    @CrossOrigin
    @GetMapping("/campaigns/{id}")
    public List<InviteResponse> getAllInvitesForCampaign(@PathVariable int id) {
        return inviteService.getAllInvitesForCampaign(id);
    }

    @CrossOrigin
    @PostMapping("/{id}/accept")
    public void acceptInvite(@PathVariable int id) {
        inviteService.acceptInvite(id);
    }

    @CrossOrigin
    @PostMapping("/{id}/decline")
    public void declineInvite(@PathVariable int id) {
        inviteService.declineInvite(id);
    }

    @CrossOrigin
    @PostMapping("/{id}/revoke")
    public void revokeInvite(@PathVariable int id) {
        inviteService.revokeInvite(id);
    }
}
