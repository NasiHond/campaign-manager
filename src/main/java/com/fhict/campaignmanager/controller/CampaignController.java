package com.fhict.campaignmanager.controller;

import com.fhict.campaignmanager.domain.Campaign;
import com.fhict.campaignmanager.dto.CampaignResponse;
import com.fhict.campaignmanager.dto.CreateCampaignRequest;
import com.fhict.campaignmanager.dto.UpdateCampaignRequest;
import com.fhict.campaignmanager.mapper.CampaignMapper;
import com.fhict.campaignmanager.mapper.InviteMapper;
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
    private final InviteMapper inviteMapper;

    public CampaignController(ICampaignService campaignService, CampaignMapper campaignMapper,
                              IInviteService inviteService, InviteMapper inviteMapper) {
        this.campaignService = campaignService;
        this.campaignMapper = campaignMapper;
        this.inviteService = inviteService;
        this.inviteMapper = inviteMapper;
    }

    @PostMapping
    public CampaignResponse createCampaign(@RequestBody CreateCampaignRequest createCampaignRequest) {
        return campaignMapper.toCampaignResponse(campaignService.createCampaign(createCampaignRequest));
    }

    @GetMapping("/{id}")
    public CampaignResponse getCampaign(@PathVariable int id) {
        CampaignResponse campaign = campaignMapper.toCampaignResponse(campaignService.getCampaign(id));
        if (campaign != null) {
            campaign.setInvites(inviteService.getAllInvitesForCampaign(id).stream()
                    .map(inviteMapper::toInviteResponse)
                    .toList());
        }
        return campaign;
    }

    @GetMapping
    public List<CampaignResponse> getCampaigns() {
        List<Campaign> campaigns = campaignService.getAllCampaignsFromUser();
        if (campaigns == null) {
            return null;
        }
        return campaigns.stream()
                .map(campaignMapper::toCampaignResponse)
                .toList();
    }

    @PutMapping("/{id}/participants/{userId}/role")
    public CampaignResponse updateParticipantRole(@PathVariable int id, @PathVariable int userId, @RequestParam String role) {
        return campaignMapper.toCampaignResponse(
                campaignService.updateParticipantRole(id, userId, role));
    }

    @PutMapping("/{id}/participants/{userId}")
    public void removeParticipant(@PathVariable int id, @PathVariable int userId) {
        campaignService.removeParticipant(id, userId);
    }

    @PutMapping("/{id}")
    public CampaignResponse updateCampaign(@PathVariable int id, @RequestBody UpdateCampaignRequest updateCreateCampaignRequest) {
        return campaignMapper.toCampaignResponse(
                campaignService.updateCampaign(id, updateCreateCampaignRequest));
    }

    @DeleteMapping("/{id}")
    public void deleteCampaign(@PathVariable int id) {
        campaignService.deleteCampaign(id);
    }
}
