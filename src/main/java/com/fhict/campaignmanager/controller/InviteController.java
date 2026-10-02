package com.fhict.campaignmanager.controller;

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
    public void sendInvite() {
        // Implement the logic to send an invite
    }

    @CrossOrigin
    @GetMapping("/{id}")
    public void getInvite(@PathVariable int id) {
        // Implement the logic to retrieve an invite by ID
    }

    @CrossOrigin
    @GetMapping
    public List<InviteResponse> getAllInvites() {
        return inviteService.getAllInvitesForUser();
    }

    @CrossOrigin
    @GetMapping("/campaigns/{id}")
    public List<InviteResponse> getAllInvitesForCampaign(@PathVariable int id) {
        // Implement the logic to retrieve all invites for a specific campaign
        return null;
    }
}
