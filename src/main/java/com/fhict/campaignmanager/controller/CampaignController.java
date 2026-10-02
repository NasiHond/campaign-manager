package com.fhict.campaignmanager.controller;

import com.fhict.campaignmanager.dto.CampaignResponse;
import com.fhict.campaignmanager.dto.CreateCampaignRequest;
import com.fhict.campaignmanager.mapper.CampaignMapper;
import com.fhict.campaignmanager.service.CampaignService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {
    private final CampaignService campaignService;
    private final CampaignMapper campaignMapper;

    public CampaignController(CampaignService campaignService, CampaignMapper campaignMapper) {
        this.campaignService = campaignService;
        this.campaignMapper = campaignMapper;
    }

    @CrossOrigin
    @PostMapping
    public CampaignResponse createCampaign(@RequestBody CreateCampaignRequest createCampaignRequest) {
        return campaignService.createCampaign(createCampaignRequest);
    }

    @CrossOrigin
    @GetMapping("/{id}")
    public CampaignResponse getCampaign(@PathVariable int id) {
        // Implement the logic to retrieve a campaign by ID and return the response
        return null; // Placeholder for actual implementation
    }

    @CrossOrigin
    @GetMapping
    public List<CampaignResponse> getCampaigns() {
        return campaignService.getAllCampaignsFromUser();
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
