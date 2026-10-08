package com.fhict.campaignmanager.controller;

import com.fhict.campaignmanager.dto.CampaignResponse;
import com.fhict.campaignmanager.dto.CreateCampaignRequest;
import com.fhict.campaignmanager.dto.UpdateCampaignRequest;
import com.fhict.campaignmanager.mapper.CampaignMapper;
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
    public CampaignResponse updateParticipantRole(@PathVariable int id, @PathVariable int userId, @RequestParam String role) {
        return campaignService.updateParticipantRole(id, userId, role);
    }

    @CrossOrigin
    @PutMapping("/{id}/participants/{userId}")
    public void removeParticipant(@PathVariable int id, @PathVariable int userId) {
        campaignService.removeParticipant(id, userId);
    }

    @CrossOrigin
    @PutMapping("/{id}")
    public CampaignResponse updateCampaign(@PathVariable int id, @RequestBody UpdateCampaignRequest updateCreateCampaignRequest) {
        return campaignService.updateCampaign(id, updateCreateCampaignRequest);
    }

    @CrossOrigin
    @DeleteMapping("/{id}")
    public void deleteCampaign(@PathVariable int id) {
        campaignService.deleteCampaign(id);
    }
}
