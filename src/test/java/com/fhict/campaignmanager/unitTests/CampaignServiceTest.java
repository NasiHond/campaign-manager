package com.fhict.campaignmanager.unitTests;

import com.fhict.campaignmanager.domain.Campaign;
import com.fhict.campaignmanager.domain.Role;
import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.dto.CampaignResponse;
import com.fhict.campaignmanager.dto.CreateCampaignRequest;
import com.fhict.campaignmanager.mapper.CampaignMapper;
import com.fhict.campaignmanager.repository.CampaignRepository;
import com.fhict.campaignmanager.service.CampaignService;
import com.fhict.campaignmanager.service.IAuthService;
import com.fhict.campaignmanager.service.IUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock private CampaignRepository campaignRepository;
    @Mock private IAuthService authService;
    @Mock private IUserService userService;
    @Mock private CampaignMapper campaignMapper;

    private CampaignService campaignService;

    @BeforeEach
    void setUp() {
        campaignService = new CampaignService(
                campaignRepository, authService, userService, campaignMapper);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createCampaign_withValidAuthenticationSavesOwnerCampaign() {
        setAuthenticatedUser("robin");
        User creator = User.builder().username("robin").build();
        CreateCampaignRequest request = CreateCampaignRequest.builder()
                .name("Campaign")
                .description("Description")
                .build();
        Campaign saved = Campaign.builder().id(1).name("Campaign")
                .description("Description")
                .participants(Map.of(creator, Role.OWNER))
                .build();
        CampaignResponse expected = CampaignResponse.builder().id(1).name("Campaign")
                .description("Description")
                .participants(saved.getParticipants())
                .build();

        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("robin")).thenReturn(creator);
        when(campaignRepository.save(any(Campaign.class))).thenReturn(saved);
        when(campaignMapper.toCampaignResponse(saved)).thenReturn(expected);

        CampaignResponse result = campaignService.createCampaign(request);

        assertEquals(expected, result);
        verify(campaignRepository).save(argThat(campaign ->
                "Campaign".equals(campaign.getName())
                        && "Description".equals(campaign.getDescription())
                        && Role.OWNER.equals(campaign.getParticipants().get(creator))));
    }

    @Test
    void createCampaign_withoutValidAuthentication_returnsNull() {
        setAuthenticatedUser("robin");
        when(authService.isAuthenticationValid(any())).thenReturn(false);

        assertNull(campaignService.createCampaign(CreateCampaignRequest.builder()
                .name("Campaign")
                .description("Description")
                .build()));
        verifyNoInteractions(userService, campaignRepository, campaignMapper);
    }

    @Test
    void getAllCampaignsFromUser_returnsMappedCampaigns() {
        setAuthenticatedUser("robin");
        User user = User.builder().username("robin").build();
        Campaign first = Campaign.builder().id(1).name("First").build();
        Campaign second = Campaign.builder().id(2).name("Second").build();
        CampaignResponse firstResponse = CampaignResponse.builder().id(1).name("First").build();
        CampaignResponse secondResponse = CampaignResponse.builder().id(2).name("Second").build();

        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("robin")).thenReturn(user);
        when(campaignRepository.findAllByParticipantsContainingKey(user))
                .thenReturn(List.of(first, second));
        when(campaignMapper.toCampaignResponse(first)).thenReturn(firstResponse);
        when(campaignMapper.toCampaignResponse(second)).thenReturn(secondResponse);

        assertEquals(List.of(firstResponse, secondResponse),
                campaignService.getAllCampaignsFromUser());
    }

    @Test
    void getAllCampaignsFromUser_withoutAuthentication_returnsNull() {
        assertNull(campaignService.getAllCampaignsFromUser());
        verifyNoInteractions(authService, userService, campaignRepository, campaignMapper);
    }

    @Test
    void updateParticipantRole_asOwner_updatesParticipantAndReturnsCampaign() {
        setAuthenticatedUser("owner");
        User owner = User.builder().id(1).username("owner").build();
        User participant = User.builder().id(2).username("participant").build();
        Campaign campaign = campaignWithParticipants(owner, participant);
        CampaignResponse expected = CampaignResponse.builder().id(10).build();

        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("owner")).thenReturn(owner);
        when(campaignRepository.findById(10)).thenReturn(campaign);
        when(userService.getUserById(2)).thenReturn(participant);
        when(campaignRepository.save(campaign)).thenReturn(campaign);
        when(campaignMapper.toCampaignResponse(campaign)).thenReturn(expected);

        assertEquals(expected, campaignService.updateParticipantRole(10, 2, "AUTHOR"));
        assertEquals(Role.AUTHOR, campaign.getParticipants().get(participant));
        verify(campaignRepository).save(campaign);
    }

    @Test
    void updateParticipantRole_withoutValidAuthentication_returnsNull() {
        setAuthenticatedUser("owner");
        when(authService.isAuthenticationValid(any())).thenReturn(false);

        assertNull(campaignService.updateParticipantRole(10, 2, "AUTHOR"));
        verifyNoInteractions(campaignRepository, userService, campaignMapper);
    }

    @Test
    void updateParticipantRole_asNonOwner_throwsException() {
        setAuthenticatedUser("participant");
        User owner = User.builder().id(1).username("owner").build();
        User participant = User.builder().id(2).username("participant").build();
        Campaign campaign = campaignWithParticipants(owner, participant);

        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("participant")).thenReturn(participant);
        when(campaignRepository.findById(10)).thenReturn(campaign);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> campaignService.updateParticipantRole(10, 2, "AUTHOR"));

        assertEquals("Only the owner of the campaign can update participant roles.",
                exception.getMessage());
        verify(campaignRepository, never()).save(any());
    }

    @Test
    void updateParticipantRole_forNonParticipant_throwsException() {
        setAuthenticatedUser("owner");
        User owner = User.builder().id(1).username("owner").build();
        User participant = User.builder().id(2).username("participant").build();
        Campaign campaign = campaignWithParticipants(owner, participant);

        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("owner")).thenReturn(owner);
        when(campaignRepository.findById(10)).thenReturn(campaign);
        when(userService.getUserById(3)).thenReturn(User.builder().id(3).build());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> campaignService.updateParticipantRole(10, 3, "AUTHOR"));

        assertEquals("User is not a participant of the campaign.", exception.getMessage());
        verify(campaignRepository, never()).save(any());
    }

    @Test
    void removeParticipant_asOwner_removesParticipantAndSavesCampaign() {
        setAuthenticatedUser("owner");
        User owner = User.builder().id(1).username("owner").build();
        User participant = User.builder().id(2).username("participant").build();
        Campaign campaign = campaignWithParticipants(owner, participant);

        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("owner")).thenReturn(owner);
        when(campaignRepository.findById(10)).thenReturn(campaign);
        when(userService.getUserById(2)).thenReturn(participant);

        campaignService.removeParticipant(10, 2);

        assertFalse(campaign.getParticipants().containsKey(participant));
        assertEquals(Role.OWNER, campaign.getParticipants().get(owner));
        verify(campaignRepository).save(campaign);
    }

    @Test
    void removeParticipant_withoutValidAuthentication_doesNothing() {
        setAuthenticatedUser("owner");
        when(authService.isAuthenticationValid(any())).thenReturn(false);

        campaignService.removeParticipant(10, 2);

        verifyNoInteractions(campaignRepository, userService);
    }

    @Test
    void removeParticipant_asNonOwner_throwsException() {
        setAuthenticatedUser("participant");
        User owner = User.builder().id(1).username("owner").build();
        User participant = User.builder().id(2).username("participant").build();
        Campaign campaign = campaignWithParticipants(owner, participant);

        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("participant")).thenReturn(participant);
        when(campaignRepository.findById(10)).thenReturn(campaign);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> campaignService.removeParticipant(10, 2));

        assertEquals("Only the owner of the campaign can remove participants.",
                exception.getMessage());
        verify(campaignRepository, never()).save(any());
    }

    @Test
    void removeParticipant_forNonParticipant_throwsException() {
        setAuthenticatedUser("owner");
        User owner = User.builder().id(1).username("owner").build();
        User participant = User.builder().id(2).username("participant").build();
        Campaign campaign = campaignWithParticipants(owner, participant);

        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("owner")).thenReturn(owner);
        when(campaignRepository.findById(10)).thenReturn(campaign);
        when(userService.getUserById(3)).thenReturn(User.builder().id(3).build());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> campaignService.removeParticipant(10, 3));

        assertEquals("User is not a participant of the campaign.", exception.getMessage());
        verify(campaignRepository, never()).save(any());
    }

    @Test
    void updateCampaign_asOwner_updatesCampaignAndReturnsResponse() {
        setAuthenticatedUser("owner");
        User owner = User.builder().id(1).username("owner").build();
        Campaign campaign = campaignWithParticipants(owner, User.builder().id(2).username("participant").build());
        CampaignResponse expected = CampaignResponse.builder().id(10).name("Updated").description("Updated description").build();

        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("owner")).thenReturn(owner);
        when(campaignRepository.findById(10)).thenReturn(campaign);
        when(campaignRepository.save(campaign)).thenReturn(campaign);
        when(campaignMapper.toCampaignResponse(campaign)).thenReturn(expected);

        CampaignResponse result = campaignService.updateCampaign(10,
                com.fhict.campaignmanager.dto.UpdateCampaignRequest.builder()
                        .name("Updated")
                        .description("Updated description")
                        .build());

        assertEquals(expected, result);
        assertEquals("Updated", campaign.getName());
        assertEquals("Updated description", campaign.getDescription());
    }

    @Test
    void updateCampaign_withoutValidAuthentication_returnsNull() {
        setAuthenticatedUser("owner");
        when(authService.isAuthenticationValid(any())).thenReturn(false);

        assertNull(campaignService.updateCampaign(10,
                com.fhict.campaignmanager.dto.UpdateCampaignRequest.builder()
                        .name("Updated")
                        .description("Updated description")
                        .build()));
        verifyNoInteractions(userService, campaignRepository, campaignMapper);
    }

    @Test
    void updateCampaign_asNonOwner_returnsNull() {
        setAuthenticatedUser("participant");
        User owner = User.builder().id(1).username("owner").build();
        User participant = User.builder().id(2).username("participant").build();
        Campaign campaign = campaignWithParticipants(owner, participant);

        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("participant")).thenReturn(participant);
        when(campaignRepository.findById(10)).thenReturn(campaign);

        assertNull(campaignService.updateCampaign(10,
                com.fhict.campaignmanager.dto.UpdateCampaignRequest.builder()
                        .name("Updated")
                        .description("Updated description")
                        .build()));
        verify(campaignRepository, never()).save(any());
    }

    @Test
    void deleteCampaign_asOwner_deletesCampaign() {
        setAuthenticatedUser("owner");
        User owner = User.builder().id(1).username("owner").build();
        Campaign campaign = campaignWithParticipants(owner, User.builder().id(2).username("participant").build());

        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("owner")).thenReturn(owner);
        when(campaignRepository.findById(10)).thenReturn(campaign);

        campaignService.deleteCampaign(10);

        verify(campaignRepository).delete(campaign.getId());
    }

    @Test
    void deleteCampaign_withoutValidAuthentication_doesNothing() {
        setAuthenticatedUser("owner");
        when(authService.isAuthenticationValid(any())).thenReturn(false);

        campaignService.deleteCampaign(10);

        verifyNoInteractions(campaignRepository, userService);
    }

    @Test
    void deleteCampaign_asNonOwner_throwsException() {
        setAuthenticatedUser("participant");
        User owner = User.builder().id(1).username("owner").build();
        User participant = User.builder().id(2).username("participant").build();
        Campaign campaign = campaignWithParticipants(owner, participant);

        when(authService.isAuthenticationValid(any())).thenReturn(true);
        when(userService.getUserByUsername("participant")).thenReturn(participant);
        when(campaignRepository.findById(10)).thenReturn(campaign);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> campaignService.deleteCampaign(10));

        assertEquals("Only the owner of the campaign can delete it.", exception.getMessage());
        verify(campaignRepository, never()).delete(anyInt());
    }

    private Campaign campaignWithParticipants(User owner, User participant) {
        return Campaign.builder()
                .id(10)
                .name("Campaign")
                .participants(new java.util.HashMap<>(Map.of(
                        owner, Role.OWNER,
                        participant, Role.PARTICIPANT)))
                .build();
    }

    private void setAuthenticatedUser(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, "credentials",
                        List.of(() -> "ROLE_USER")));
    }
}
