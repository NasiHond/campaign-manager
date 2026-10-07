package com.fhict.campaignmanager.unitTests;

import com.fhict.campaignmanager.domain.Campaign;
import com.fhict.campaignmanager.domain.CampaignInvitation;
import com.fhict.campaignmanager.domain.InvitationStatus;
import com.fhict.campaignmanager.domain.Role;
import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.dto.CreateInviteRequest;
import com.fhict.campaignmanager.dto.InviteResponse;
import com.fhict.campaignmanager.mapper.InviteMapper;
import com.fhict.campaignmanager.repository.CampaignRepository;
import com.fhict.campaignmanager.repository.InviteRepository;
import com.fhict.campaignmanager.service.IAuthService;
import com.fhict.campaignmanager.service.IUserService;
import com.fhict.campaignmanager.service.InviteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InviteServiceTest {

    @Mock private InviteRepository inviteRepository;
    @Mock private CampaignRepository campaignRepository;
    @Mock private IAuthService authService;
    @Mock private IUserService userService;
    @Mock private InviteMapper inviteMapper;

    private InviteService inviteService;
    private User owner;
    private User invitedUser;
    private Campaign campaign;

    @BeforeEach
    void setUp() {
        inviteService = new InviteService(
                inviteRepository, campaignRepository, authService, userService, inviteMapper);
        owner = User.builder().id(1).username("owner").build();
        invitedUser = User.builder().id(2).username("invitee").email("invitee@example.com").build();
        campaign = Campaign.builder()
                .id(10)
                .name("Campaign")
                .participants(new HashMap<>(Map.of(owner, Role.OWNER)))
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void sendInvite_withUsernameAndOwnerAuthentication_savesPendingInvite() {
        authenticateAs("owner");
        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(campaignRepository.findById(10)).thenReturn(campaign);
        when(userService.getUserByUsername("invitee")).thenReturn(invitedUser);
        when(userService.getUserByUsername("owner")).thenReturn(owner);

        inviteService.sendInvite(CreateInviteRequest.builder()
                .campaignId(10)
                .identifier("invitee")
                .role("AUTHOR")
                .build());

        ArgumentCaptor<CampaignInvitation> captor = ArgumentCaptor.forClass(CampaignInvitation.class);
        verify(inviteRepository).save(captor.capture());
        CampaignInvitation saved = captor.getValue();
        assertEquals(campaign, saved.getCampaign());
        assertEquals(owner, saved.getInvitedBy());
        assertEquals(invitedUser, saved.getInvitedUser());
        assertEquals(Role.AUTHOR, saved.getRole());
        assertEquals(InvitationStatus.PENDING, saved.getStatus());
        assertNotNull(saved.getExpiresAt());
        assertTrueAfterNow(saved.getExpiresAt());
    }

    @Test
    void sendInvite_withEmail_looksUpUserByEmail() {
        authenticateAs("owner");
        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(campaignRepository.findById(10)).thenReturn(campaign);
        when(userService.getUserByEmail("invitee@example.com")).thenReturn(invitedUser);
        when(userService.getUserByUsername("owner")).thenReturn(owner);

        inviteService.sendInvite(CreateInviteRequest.builder()
                .campaignId(10)
                .identifier("invitee@example.com")
                .role("PARTICIPANT")
                .build());

        verify(userService).getUserByEmail("invitee@example.com");
        verify(userService, never()).getUserByUsername("invitee@example.com");
        verify(inviteRepository).save(any(CampaignInvitation.class));
    }

    @Test
    void sendInvite_rejectsUnauthenticatedRequests() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> inviteService.sendInvite(CreateInviteRequest.builder().build()));

        assertEquals("Authentication is required to send an invite", exception.getMessage());
        verifyNoInteractions(campaignRepository, inviteRepository, userService);
    }

    @Test
    void sendInvite_rejectsNonOwner() {
        authenticateAs("invitee");
        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(campaignRepository.findById(10)).thenReturn(campaign);
        when(userService.getUserByUsername("invitee")).thenReturn(invitedUser);
        when(userService.getUserByUsername("invitee")).thenReturn(invitedUser);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> inviteService.sendInvite(CreateInviteRequest.builder()
                        .campaignId(10).identifier("invitee").role("EDITOR").build()));

        assertEquals("Only campaign owners can send invites", exception.getMessage());
        verify(inviteRepository, never()).save(any());
    }

    @Test
    void sendInvite_rejectsMissingUserOrCampaign() {
        authenticateAs("owner");
        when(authService.isAuthenticationValid(any())).thenReturn(true);

        when(campaignRepository.findById(99)).thenReturn(null);
        IllegalArgumentException missingCampaign = assertThrows(IllegalArgumentException.class,
                () -> inviteService.sendInvite(CreateInviteRequest.builder()
                        .campaignId(99).identifier("invitee").role("EDITOR").build()));
        assertEquals("Campaign does not exist", missingCampaign.getMessage());

        when(campaignRepository.findById(10)).thenReturn(campaign);
        when(userService.getUserByUsername("invitee")).thenReturn(null);
        IllegalArgumentException missingUser = assertThrows(IllegalArgumentException.class,
                () -> inviteService.sendInvite(CreateInviteRequest.builder()
                        .campaignId(10).identifier("invitee").role("EDITOR").build()));
        assertEquals("User does not exist", missingUser.getMessage());
    }

    @Test
    void getInvite_forInvitedUser_returnsMappedResponse() {
        authenticateAs("invitee");
        CampaignInvitation invitation = invitation(InvitationStatus.PENDING, Instant.now().plusSeconds(60));
        InviteResponse response = InviteResponse.builder().id(5).role(Role.AUTHOR).build();
        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(inviteRepository.findById(5)).thenReturn(invitation);
        when(userService.getUserByUsername("invitee")).thenReturn(invitedUser);
        when(inviteMapper.toInviteResponse(invitation)).thenReturn(response);

        assertEquals(response, inviteService.getInvite(5));
        verify(inviteMapper).toInviteResponse(invitation);
    }

    @Test
    void getInvite_returnsNullForUnknownInviteOrUnauthorizedUser() {
        authenticateAs("invitee");
        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(inviteRepository.findById(99)).thenReturn(null);
        assertNull(inviteService.getInvite(99));

        CampaignInvitation invitation = invitation(InvitationStatus.PENDING, Instant.now().plusSeconds(60));
        when(inviteRepository.findById(5)).thenReturn(invitation);
        when(userService.getUserByUsername("invitee")).thenReturn(
                User.builder().id(3).username("other").build());
        assertNull(inviteService.getInvite(5));
        verifyNoInteractions(inviteMapper);
    }

    @Test
    void getAllInvitesForUser_expiresPendingInvitesAndMapsResults() {
        authenticateAs("invitee");
        CampaignInvitation expired = invitation(InvitationStatus.PENDING, Instant.now().minusSeconds(1));
        CampaignInvitation active = invitation(InvitationStatus.PENDING, Instant.now().plusSeconds(60));
        InviteResponse expiredResponse = InviteResponse.builder().id(1).build();
        InviteResponse activeResponse = InviteResponse.builder().id(2).build();
        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("invitee")).thenReturn(invitedUser);
        when(inviteRepository.findAllByUserId(2)).thenReturn(List.of(expired, active));
        when(inviteMapper.toInviteResponse(expired)).thenReturn(expiredResponse);
        when(inviteMapper.toInviteResponse(active)).thenReturn(activeResponse);

        assertEquals(List.of(expiredResponse, activeResponse), inviteService.getAllInvitesForUser());
        assertEquals(InvitationStatus.EXPIRED, expired.getStatus());
        verify(inviteRepository).save(expired);
    }

    @Test
    void getAllInvitesForCampaign_returnsInvitesOnlyForOwner() {
        authenticateAs("owner");
        CampaignInvitation invitation = invitation(InvitationStatus.PENDING, Instant.now().plusSeconds(60));
        InviteResponse response = InviteResponse.builder().id(1).build();
        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(campaignRepository.findById(10)).thenReturn(campaign);
        when(userService.getUserByUsername("owner")).thenReturn(owner);
        when(inviteRepository.findAllByCampaignId(10)).thenReturn(List.of(invitation));
        when(inviteMapper.toInviteResponse(invitation)).thenReturn(response);

        assertEquals(List.of(response), inviteService.getAllInvitesForCampaign(10));

        campaign.setParticipants(new HashMap<>(Map.of(invitedUser, Role.AUTHOR)));
        assertEquals(List.of(), inviteService.getAllInvitesForCampaign(10));
        verify(inviteRepository).findAllByCampaignId(10);
    }

    @Test
    void acceptInvite_updatesStatusAndAddsUserToCampaign() {
        authenticateAs("invitee");
        CampaignInvitation invitation = invitation(InvitationStatus.PENDING, Instant.now().plusSeconds(60));
        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(inviteRepository.findById(5)).thenReturn(invitation);
        when(userService.getUserByUsername("invitee")).thenReturn(invitedUser);

        inviteService.acceptInvite(5);

        assertEquals(InvitationStatus.ACCEPTED, invitation.getStatus());
        assertEquals(Role.AUTHOR, campaign.getParticipants().get(invitedUser));
        verify(inviteRepository).save(invitation);
        verify(campaignRepository).save(campaign);
    }

    @Test
    void acceptInvite_rejectsExpiredOrAlreadyHandledInvite() {
        authenticateAs("invitee");
        CampaignInvitation invitation = invitation(InvitationStatus.PENDING, Instant.now().minusSeconds(1));
        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(inviteRepository.findById(5)).thenReturn(invitation);
        when(userService.getUserByUsername("invitee")).thenReturn(invitedUser);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> inviteService.acceptInvite(5));

        assertEquals("Invite is not valid for acceptance. Current status: EXPIRED", exception.getMessage());
        verify(campaignRepository, never()).save(any());
    }

    @Test
    void declineInvite_marksInviteAsDeclined() {
        authenticateAs("invitee");
        CampaignInvitation invitation = invitation(InvitationStatus.PENDING, Instant.now().plusSeconds(60));
        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(inviteRepository.findById(5)).thenReturn(invitation);
        when(userService.getUserByUsername("invitee")).thenReturn(invitedUser);

        inviteService.declineInvite(5);

        assertEquals(InvitationStatus.DECLINED, invitation.getStatus());
        verify(inviteRepository).save(invitation);
    }

    @Test
    void revokeInvite_marksPendingInviteAsRevoked() {
        authenticateAs("owner");
        CampaignInvitation invitation = invitation(InvitationStatus.PENDING, Instant.now().plusSeconds(60));
        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(inviteRepository.findById(5)).thenReturn(invitation);
        when(userService.getUserByUsername("owner")).thenReturn(owner);

        inviteService.revokeInvite(5);

        assertEquals(InvitationStatus.REVOKED, invitation.getStatus());
        verify(inviteRepository).save(invitation);
    }

    @Test
    void revokeInvite_rejectsNonInviterAndNonPendingInvite() {
        authenticateAs("invitee");
        CampaignInvitation invitation = invitation(InvitationStatus.PENDING, Instant.now().plusSeconds(60));
        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(inviteRepository.findById(5)).thenReturn(invitation);
        when(userService.getUserByUsername("invitee")).thenReturn(invitedUser);

        IllegalArgumentException unauthorized = assertThrows(IllegalArgumentException.class,
                () -> inviteService.revokeInvite(5));
        assertEquals("Only the inviter can revoke this invite", unauthorized.getMessage());

        authenticateAs("owner");
        invitation.setStatus(InvitationStatus.ACCEPTED);
        when(userService.getUserByUsername("owner")).thenReturn(owner);
        IllegalArgumentException handled = assertThrows(IllegalArgumentException.class,
                () -> inviteService.revokeInvite(5));
        assertEquals("Invite is not valid for revocation. Current status: ACCEPTED", handled.getMessage());
    }

    private CampaignInvitation invitation(InvitationStatus status, Instant expiresAt) {
        return CampaignInvitation.builder()
                .id(5)
                .campaign(campaign)
                .invitedBy(owner)
                .invitedUser(invitedUser)
                .role(Role.AUTHOR)
                .status(status)
                .expiresAt(expiresAt)
                .build();
    }

    private void authenticateAs(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, "credentials", List.of()));
    }

    private void assertTrueAfterNow(Instant instant) {
        assertFalse(instant.isBefore(Instant.now()));
    }
}
