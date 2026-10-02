package com.fhict.campaignmanager.service;

import com.fhict.campaignmanager.domain.CampaignInvitation;
import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.dto.InviteResponse;
import com.fhict.campaignmanager.mapper.InviteMapper;
import com.fhict.campaignmanager.repository.InviteRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InviteService implements IInviteService{

    private final InviteRepository inviteRepository;
    private final IAuthService authService;
    private final IUserService userService;
    private final InviteMapper inviteMapper;

    public InviteService(InviteRepository inviteRepository, IAuthService authService, IUserService userService, InviteMapper inviteMapper) {
        this.inviteRepository = inviteRepository;
        this.authService = authService;
        this.userService = userService;
        this.inviteMapper = inviteMapper;
    }

    @Override
    public void sendInvite(int campaignId, String identifier) {
    }

    @Override
    public InviteResponse getInvite(int inviteId) {
        return null;
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
                .map(inviteMapper::toInviteResponse)
                .toList();
    }

    @Override
    public List<InviteResponse> getAllInvitesForCampaign(int campaignId) {
        return List.of();
    }
}
