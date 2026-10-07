package com.fhict.campaignmanager.controller;

import com.fhict.campaignmanager.dto.CampaignResponse;
import com.fhict.campaignmanager.dto.CreateCampaignRequest;
import com.fhict.campaignmanager.mapper.CampaignMapper;
import com.fhict.campaignmanager.service.CampaignService;
import com.fhict.campaignmanager.service.ICampaignService;
import com.fhict.campaignmanager.service.IInviteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {
    private final ICampaignService campaignService;
    private final CampaignMapper campaignMapper;
    private final IInviteService inviteService;

    public CampaignController(ICampaignService campaignService, CampaignMapper campaignMapper,
                              IInviteService inviteService) {
        this.campaignService = campaignService;
        this.campaignMapper = campaignMapper;
        this.inviteService = inviteService;
    }

    @CrossOrigin
    @PostMapping
    public CampaignResponse createCampaign(@RequestBody CreateCampaignRequest createCampaignRequest) {
        return campaignService.createCampaign(createCampaignRequest);
    }

    @CrossOrigin
    @GetMapping("/{id}")
    public CampaignResponse getCampaign(@PathVariable int id) {
        CampaignResponse campaign = campaignService.getCampaign(id);
        if (campaign != null) {
            campaign.setInvites(inviteService.getAllInvitesForCampaign(id));
        }
        return campaign;
    }

    @CrossOrigin
    @GetMapping
    public List<CampaignResponse> getCampaigns() {
        return campaignService.getAllCampaignsFromUser();
    }

    @CrossOrigin
    @PutMapping("/{id}/participants/{userId}/role")
    public void updateParticipantRole(@PathVariable int id, @PathVariable int userId, @RequestParam String role) {
        campaignService.updateParticipantRole(id, userId, role);
    }

    @CrossOrigin
    @PutMapping("/{id}/participants/{userId}")
    public void removeParticipant(@PathVariable int id, @PathVariable int userId) {
        // Implement the logic to update a participant
    }

    @CrossOrigin
    @PutMapping("/{id}")
    public CampaignResponse updateCampaign(@PathVariable int id, @RequestBody CreateCampaignRequest updateCampaignRequest) {
        // Implement the logic to update a campaign and return the response
        return null; // Placeholder for actual implementation
    }

    @CrossOrigin
    @DeleteMapping("/{id}")
    public void deleteCampaign(@PathVariable int id) {
        // Implement the logic to delete a campaign
    }
}
