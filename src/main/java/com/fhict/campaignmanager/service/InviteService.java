package com.fhict.campaignmanager.service;

import com.fhict.campaignmanager.domain.CampaignInvitation;
import com.fhict.campaignmanager.domain.Campaign;
import com.fhict.campaignmanager.domain.InvitationStatus;
import com.fhict.campaignmanager.domain.Role;
import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.dto.CreateInviteRequest;
import com.fhict.campaignmanager.dto.InviteResponse;
import com.fhict.campaignmanager.mapper.InviteMapper;
import com.fhict.campaignmanager.repository.CampaignRepository;
import com.fhict.campaignmanager.repository.InviteRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class InviteService implements IInviteService{

    private static final Duration INVITE_EXPIRATION = Duration.ofDays(7);

    private final InviteRepository inviteRepository;
    private final CampaignRepository campaignRepository;
    private final IAuthService authService;
    private final IUserService userService;
    private final InviteMapper inviteMapper;
    private final CampaignAuthorizationService campaignAuthorizationService;

    @Autowired
    public InviteService(InviteRepository inviteRepository, CampaignRepository campaignRepository,
                         IAuthService authService, IUserService userService, InviteMapper inviteMapper,
                         CampaignAuthorizationService campaignAuthorizationService) {
        this.inviteRepository = inviteRepository;
        this.campaignRepository = campaignRepository;
        this.authService = authService;
        this.userService = userService;
        this.inviteMapper = inviteMapper;
        this.campaignAuthorizationService = campaignAuthorizationService;
    }

    public InviteService(InviteRepository inviteRepository, CampaignRepository campaignRepository,
                         IAuthService authService, IUserService userService, InviteMapper inviteMapper) {
        this(inviteRepository, campaignRepository, authService, userService, inviteMapper,
                new CampaignAuthorizationService());
    }

    @Override
    public void sendInvite(CreateInviteRequest inviteRequest) {
        int campaignId = inviteRequest.getCampaignId();
        String identifier = inviteRequest.getIdentifier();
        String role = inviteRequest.getRole();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            throw new IllegalStateException("Authentication is required to send an invite");
        }
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("User identifier is required");
        }

        Campaign campaign = campaignRepository.findById(campaignId);
        if (campaign == null) {
            throw new IllegalArgumentException("Campaign does not exist");
        }

        User invitedUser = identifier.contains("@")
                ? userService.getUserByEmail(identifier)
                : userService.getUserByUsername(identifier);
        if (invitedUser == null) {
            throw new IllegalArgumentException("User does not exist");
        }

        User invitedBy = userService.getUserByUsername(authentication.getName());
        if (!campaignAuthorizationService.isOwner(campaign, invitedBy)) {
            throw new IllegalArgumentException("Only campaign owners can send invites");
        }

        CampaignInvitation invitation = CampaignInvitation.builder()
                .campaign(campaign)
                .invitedBy(invitedBy)
                .invitedUser(invitedUser)
                .role(Role.valueOf(role))
                .status(InvitationStatus.PENDING)
                .expiresAt(Instant.now().plus(INVITE_EXPIRATION))
                .build();

        inviteRepository.save(invitation);
    }

    @Override
    public InviteResponse getInvite(int inviteId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            return null;
        }

        CampaignInvitation invitation = inviteRepository.findById(inviteId);
        if (invitation == null) {
            return null;
        }
        expireIfNecessary(invitation);

        User user = userService.getUserByUsername(authentication.getName());
        if (user == null || invitation.getInvitedUser().getId() != user.getId()) {
            return null;
        }

        return inviteMapper.toInviteResponse(invitation);
    }

    @Override
    public List<InviteResponse> getAllInvitesForUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            return null;
        }

        User user = userService.getUserByUsername(authentication.getName());
        List<CampaignInvitation> invitations = inviteRepository.findAllByUserId(user.getId());
        return invitations.stream()
                .map(invitation -> {
                    expireIfNecessary(invitation);
                    return inviteMapper.toInviteResponse(invitation);
                })
                .toList();
    }

    @Override
    public List<InviteResponse> getAllInvitesForCampaign(int campaignId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            return List.of();
        }

        Campaign campaign = campaignRepository.findById(campaignId);
        User user = userService.getUserByUsername(authentication.getName());
        if (!campaignAuthorizationService.isOwner(campaign, user)) {
            return List.of();
        }

        return inviteRepository.findAllByCampaignId(campaignId).stream()
                .map(invitation -> {
                    expireIfNecessary(invitation);
                    return inviteMapper.toInviteResponse(invitation);
                })
                .toList();
    }

    @Override
    public void acceptInvite(int inviteId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            throw new IllegalStateException("Authentication is required to accept an invite");
        }

        CampaignInvitation invitation = inviteRepository.findById(inviteId);
        if (invitation == null) {
            throw new IllegalArgumentException("Invite does not exist");
        }
        expireIfNecessary(invitation);

        User user = userService.getUserByUsername(authentication.getName());
        if (user == null || invitation.getInvitedUser().getId() != user.getId()) {
            throw new IllegalArgumentException("You are not authorized to accept this invite");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalArgumentException("Invite is not valid for acceptance. Current status: " + invitation.getStatus());
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);
        inviteRepository.save(invitation);

        Campaign campaign = invitation.getCampaign();
        campaign.getParticipants().put(user, invitation.getRole());
        campaignRepository.save(campaign);
    }

    @Override
    public void declineInvite(int inviteId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            throw new IllegalStateException("Authentication is required to decline an invite");
        }

        CampaignInvitation invitation = inviteRepository.findById(inviteId);
        if (invitation == null) {
            throw new IllegalArgumentException("Invite does not exist");
        }
        expireIfNecessary(invitation);

        User user = userService.getUserByUsername(authentication.getName());
        if (user == null || invitation.getInvitedUser().getId() != user.getId()) {
            throw new IllegalArgumentException("You are not authorized to decline this invite");
        }

        invitation.setStatus(InvitationStatus.DECLINED);
        inviteRepository.save(invitation);
    }

    @Override
    public void revokeInvite(int inviteId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            throw new IllegalStateException("Authentication is required to revoke an invite");
        }

        CampaignInvitation invitation = inviteRepository.findById(inviteId);
        if (invitation == null) {
            throw new IllegalArgumentException("Invite does not exist");
        }
        expireIfNecessary(invitation);

        User user = userService.getUserByUsername(authentication.getName());
        if (user == null || invitation.getInvitedBy().getId() != user.getId()) {
            throw new IllegalArgumentException("Only the inviter can revoke this invite");
        }
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalArgumentException("Invite is not valid for revocation. Current status: "
                    + invitation.getStatus());
        }

        invitation.setStatus(InvitationStatus.REVOKED);
        inviteRepository.save(invitation);
    }

    private void expireIfNecessary(CampaignInvitation invitation) {
        if (invitation.getStatus() == InvitationStatus.PENDING
                && invitation.getExpiresAt() != null
                && !Instant.now().isBefore(invitation.getExpiresAt())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            inviteRepository.save(invitation);
        }
    }
}
