package com.fhict.campaignmanager.service;

import com.fhict.campaignmanager.domain.CampaignInvite;
import com.fhict.campaignmanager.domain.Campaign;
import com.fhict.campaignmanager.domain.InviteStatus;
import com.fhict.campaignmanager.domain.Role;
import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.dto.CreateInviteRequest;
import com.fhict.campaignmanager.repository.CampaignRepository;
import com.fhict.campaignmanager.repository.InviteRepository;
import com.fhict.campaignmanager.mapper.DomainEntityMapper;
import com.fhict.campaignmanager.persistence.entity.CampaignInvitationEntity;
import com.fhict.campaignmanager.persistence.entity.CampaignParticipantEntity;
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
    private final CampaignAuthorizationService campaignAuthorizationService;
    private final DomainEntityMapper entityMapper;

    @Autowired
    public InviteService(InviteRepository inviteRepository, CampaignRepository campaignRepository,
                         IAuthService authService, IUserService userService,
                         CampaignAuthorizationService campaignAuthorizationService,
                         DomainEntityMapper entityMapper) {
        this.inviteRepository = inviteRepository;
        this.campaignRepository = campaignRepository;
        this.authService = authService;
        this.userService = userService;
        this.campaignAuthorizationService = campaignAuthorizationService;
        this.entityMapper = entityMapper;
    }

    public InviteService(InviteRepository inviteRepository, CampaignRepository campaignRepository,
                         IAuthService authService, IUserService userService) {
        this(inviteRepository, campaignRepository, authService, userService,
                new CampaignAuthorizationService(), new DomainEntityMapper());
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

        Campaign campaign = entityMapper.toDomain(campaignRepository.findById(campaignId).orElse(null));
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

        CampaignInvite invitation = CampaignInvite.builder()
                .campaign(campaign)
                .invitedBy(invitedBy)
                .invitedUser(invitedUser)
                .role(Role.valueOf(role))
                .status(InviteStatus.PENDING)
                .expiresAt(Instant.now().plus(INVITE_EXPIRATION))
                .build();

        inviteRepository.save(entityMapper.toEntity(invitation));
    }

    @Override
    public CampaignInvite getInvite(int inviteId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            return null;
        }

        CampaignInvite invite = entityMapper.toDomain(inviteRepository.findById(inviteId).orElse(null));
        if (invite == null) {
            return null;
        }
        expireIfNecessary(invite);

        User user = userService.getUserByUsername(authentication.getName());
        if (user == null || invite.getInvitedUser().getId() != user.getId()) {
            return null;
        }

        return invite;
    }

    @Override
    public List<CampaignInvite> getAllInvitesForUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            return null;
        }

        User user = userService.getUserByUsername(authentication.getName());
        List<CampaignInvite> invitations = inviteRepository.findAllByInvitedUser_Id(user.getId()).stream()
                .map(entityMapper::toDomain).toList();
        return invitations.stream()
                .map(invite -> {
                    expireIfNecessary(invite);
                    return invite;
                })
                .toList();
    }

    @Override
    public List<CampaignInvite> getAllInvitesForCampaign(int campaignId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            return List.of();
        }

        Campaign campaign = entityMapper.toDomain(campaignRepository.findById(campaignId).orElse(null));
        User user = userService.getUserByUsername(authentication.getName());
        if (!campaignAuthorizationService.isOwner(campaign, user)) {
            return List.of();
        }

        return inviteRepository.findAllByCampaign_Id(campaignId).stream().map(entityMapper::toDomain)
                .map(invite -> {
                    expireIfNecessary(invite);
                    return invite;
                })
                .toList();
    }

    @Override
    public void acceptInvite(int inviteId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            throw new IllegalStateException("Authentication is required to accept an invite");
        }

        CampaignInvitationEntity invitationEntity = inviteRepository.findById(inviteId).orElse(null);
        CampaignInvite invite = entityMapper.toDomain(invitationEntity);
        if (invite == null) {
            throw new IllegalArgumentException("Invite does not exist");
        }
        expireIfNecessary(invite);

        User user = userService.getUserByUsername(authentication.getName());
        if (user == null || invite.getInvitedUser().getId() != user.getId()) {
            throw new IllegalArgumentException("You are not authorized to accept this invite");
        }

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new IllegalArgumentException("Invite is not valid for acceptance. Current status: " + invite.getStatus());
        }

        invite.setStatus(InviteStatus.ACCEPTED);
        invitationEntity.setStatus(InviteStatus.ACCEPTED);
        inviteRepository.save(invitationEntity);

        var campaignEntity = invitationEntity.getCampaign();
        boolean alreadyParticipant = campaignEntity.getParticipantEntities().stream()
                .anyMatch(participant -> participant.getUser().getId() == user.getId());
        if (!alreadyParticipant) {
            campaignEntity.getParticipantEntities().add(new CampaignParticipantEntity(
                    0,
                    campaignEntity,
                    invitationEntity.getInvitedUser(),
                    invitationEntity.getRole()));
            campaignRepository.save(campaignEntity);
        }
    }

    @Override
    public void declineInvite(int inviteId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            throw new IllegalStateException("Authentication is required to decline an invite");
        }

        CampaignInvite invite = entityMapper.toDomain(inviteRepository.findById(inviteId).orElse(null));
        if (invite == null) {
            throw new IllegalArgumentException("Invite does not exist");
        }
        expireIfNecessary(invite);

        User user = userService.getUserByUsername(authentication.getName());
        if (user == null || invite.getInvitedUser().getId() != user.getId()) {
            throw new IllegalArgumentException("You are not authorized to decline this invite");
        }

        invite.setStatus(InviteStatus.DECLINED);
        inviteRepository.save(entityMapper.toEntity(invite));
    }

    @Override
    public void revokeInvite(int inviteId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authService.isAuthenticationValid(authentication)) {
            throw new IllegalStateException("Authentication is required to revoke an invite");
        }

        CampaignInvite invite = entityMapper.toDomain(inviteRepository.findById(inviteId).orElse(null));
        if (invite == null) {
            throw new IllegalArgumentException("Invite does not exist");
        }
        expireIfNecessary(invite);

        User user = userService.getUserByUsername(authentication.getName());
        if (user == null || invite.getInvitedBy().getId() != user.getId()) {
            throw new IllegalArgumentException("Only the inviter can revoke this invite");
        }
        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new IllegalArgumentException("Invite is not valid for revocation. Current status: "
                    + invite.getStatus());
        }

        invite.setStatus(InviteStatus.REVOKED);
        inviteRepository.save(entityMapper.toEntity(invite));
    }

    private void expireIfNecessary(CampaignInvite invite) {
        if (invite.getStatus() == InviteStatus.PENDING
                && invite.getExpiresAt() != null
                && !Instant.now().isBefore(invite.getExpiresAt())) {
            invite.setStatus(InviteStatus.EXPIRED);
            inviteRepository.save(entityMapper.toEntity(invite));
        }
    }
}
