package com.fhict.campaignmanager.service;

import com.fhict.campaignmanager.domain.Campaign;
import com.fhict.campaignmanager.domain.Role;
import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.dto.CampaignResponse;
import com.fhict.campaignmanager.dto.CreateCampaignRequest;
import com.fhict.campaignmanager.dto.UpdateCampaignRequest;
import com.fhict.campaignmanager.mapper.CampaignMapper;
import com.fhict.campaignmanager.repository.CampaignRepository;
import com.fhict.campaignmanager.mapper.DomainEntityMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.HashMap;

@Service
public class CampaignService implements ICampaignService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignService.class);

    private final CampaignRepository campaignRepository;
    private final IAuthService authService;
    private final IUserService userService;
    private final CampaignMapper campaignMapper;
    private final CampaignAuthorizationService campaignAuthorizationService;
    private final DomainEntityMapper entityMapper;

    @Autowired
    public CampaignService(CampaignRepository campaignRepository, IAuthService authService,
                           IUserService userService, CampaignMapper campaignMapper,
                           CampaignAuthorizationService campaignAuthorizationService,
                           DomainEntityMapper entityMapper) {
        this.campaignRepository = campaignRepository;
        this.authService = authService;
        this.userService = userService;
        this.campaignMapper = campaignMapper;
        this.campaignAuthorizationService = campaignAuthorizationService;
        this.entityMapper = entityMapper;
    }

    public CampaignService(CampaignRepository campaignRepository, IAuthService authService,
                           IUserService userService, CampaignMapper campaignMapper) {
        this(campaignRepository, authService, userService, campaignMapper,
                new CampaignAuthorizationService(), new DomainEntityMapper());
    }

    @Override
    public CampaignResponse createCampaign(CreateCampaignRequest createCampaignRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            return null;
        }
        LOGGER.info("Creating campaign for user: {}", authentication.getName());

        User creator = userService.getUserByUsername(authentication.getName());
        LOGGER.info("creator: {}", creator);
        HashMap<User, Role> participants = new HashMap<>();
        participants.put(creator, Role.OWNER);

        Campaign campaign = Campaign.builder()
                .name(createCampaignRequest.getName())
                .description(createCampaignRequest.getDescription())
                .participants(participants)
                .build();

        return campaignMapper.toCampaignResponse(entityMapper.toDomain(campaignRepository.save(entityMapper.toEntity(campaign))));
    }

    @Override
    public CampaignResponse getCampaign(int id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            return null;
        }

        Campaign campaign = entityMapper.toDomain(campaignRepository.findById(id).orElse(null));
        return campaignMapper.toCampaignResponse(campaign);
    }

    @Override
    public List<CampaignResponse> getAllCampaignsFromUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            return null;
        }
        User user = userService.getUserByUsername(authentication.getName());
        List<Campaign> campaigns = campaignRepository.findAllByParticipantEntities_User_Id(user.getId())
                .stream().map(entityMapper::toDomain).toList();
        return campaigns.stream()
                .map(campaignMapper::toCampaignResponse)
                .toList();
    }

    @Override
    public CampaignResponse updateParticipantRole(int campaignId, int userId, String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            return null;
        }

        User user = userService.getUserByUsername(authentication.getName());
        Campaign campaign = entityMapper.toDomain(campaignRepository.findById(campaignId).orElse(null));
        if (campaignAuthorizationService.isOwner(campaign, user)) {
            User participant = userService.getUserById(userId);
            if (participant != null && campaign.getParticipants().containsKey(participant)) {
                campaign.getParticipants().put(participant, Role.valueOf(role));
                return campaignMapper.toCampaignResponse(entityMapper.toDomain(campaignRepository.save(entityMapper.toEntity(campaign))));
            }
            throw new IllegalArgumentException("User is not a participant of the campaign.");
        }
        throw new IllegalArgumentException("Only the owner of the campaign can update participant roles.");
    }

    @Override
    public void removeParticipant(int campaignId, int userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            return;
        }

        User user = userService.getUserByUsername(authentication.getName());
        Campaign campaign = entityMapper.toDomain(campaignRepository.findById(campaignId).orElse(null));
        if (campaignAuthorizationService.isOwner(campaign, user)) {
            User participant = userService.getUserById(userId);
            if (participant != null && campaign.getParticipants().containsKey(participant)) {
                campaign.getParticipants().remove(participant);
                campaignRepository.save(entityMapper.toEntity(campaign));
                return;
            }
            throw new IllegalArgumentException("User is not a participant of the campaign.");
        }
        throw new IllegalArgumentException("Only the owner of the campaign can remove participants.");
    }

    @Override
    public CampaignResponse updateCampaign(int id, UpdateCampaignRequest updateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            return null;
        }

        User user = userService.getUserByUsername(authentication.getName());
        Campaign campaign = entityMapper.toDomain(campaignRepository.findById(id).orElse(null));
        if (campaignAuthorizationService.isOwner(campaign, user)) {
            campaign.setName(updateRequest.getName());
            campaign.setDescription(updateRequest.getDescription());
            return campaignMapper.toCampaignResponse(entityMapper.toDomain(campaignRepository.save(entityMapper.toEntity(campaign))));
        }
        return null;
    }

    @Override
    public void deleteCampaign(int id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            return;
        }

        User user = userService.getUserByUsername(authentication.getName());
        Campaign campaign = entityMapper.toDomain(campaignRepository.findById(id).orElse(null));
        if (campaignAuthorizationService.isOwner(campaign, user)) {
            campaignRepository.deleteById(campaign.getId());
        } else {
            throw new IllegalArgumentException("Only the owner of the campaign can delete it.");
        }
    }
}
