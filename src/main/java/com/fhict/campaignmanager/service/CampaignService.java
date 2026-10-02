package com.fhict.campaignmanager.service;

import com.fhict.campaignmanager.controller.UserController;
import com.fhict.campaignmanager.domain.Campaign;
import com.fhict.campaignmanager.domain.Role;
import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.dto.CampaignResponse;
import com.fhict.campaignmanager.dto.CreateCampaignRequest;
import com.fhict.campaignmanager.mapper.CampaignMapper;
import com.fhict.campaignmanager.repository.CampaignRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CampaignService implements ICampaignService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignService.class);

    private final CampaignRepository campaignRepository;
    private final IAuthService authService;
    private final IUserService userService;
    private final CampaignMapper campaignMapper;

    public CampaignService(CampaignRepository campaignRepository, IAuthService authService,
                           IUserService userService, CampaignMapper campaignMapper) {
        this.campaignRepository = campaignRepository;
        this.authService = authService;
        this.userService = userService;
        this.campaignMapper = campaignMapper;
    }

    @Override
    public CampaignResponse createCampaign(CreateCampaignRequest campaignRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            return null;
        }
        LOGGER.info("Creating campaign for user: {}", authentication.getName());

        User creator = userService.getUserByUsername(authentication.getName());
        LOGGER.info("creator: {}", creator);
        LOGGER.info("Role: {}", Role.OWNER);
        Campaign campaign = Campaign.builder()
                .name(campaignRequest.getName())
                .description(campaignRequest.getDescription())
                .participants(Map.of(creator, Role.OWNER))
                .build();

        return campaignMapper.toCampaignResponse(campaignRepository.save(campaign));
    }

    @Override
    public CampaignResponse getCampaign(int id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            return null;
        }
        return null;
    }

    @Override
    public List<CampaignResponse> getAllCampaignsFromUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            return null;
        }
        User user = userService.getUserByUsername(authentication.getName());
        List<Campaign> campaigns = campaignRepository.findAllByParticipantsContainingKey(user);
        return campaigns.stream()
                .map(campaignMapper::toCampaignResponse)
                .toList();
    }

    @Override
    public CampaignResponse updateCampaign(CreateCampaignRequest campaign) {
        // Implement the logic to update a campaign
        return null;
    }

    @Override
    public void deleteCampaign(int id) {
        // Implement the logic to delete a campaign by ID
    }
}
