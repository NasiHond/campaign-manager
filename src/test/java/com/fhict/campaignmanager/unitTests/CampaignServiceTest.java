package com.fhict.campaignmanager.unitTests;

import com.fhict.campaignmanager.domain.Campaign;
import com.fhict.campaignmanager.domain.Role;
import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.dto.CampaignResponse;
import com.fhict.campaignmanager.dto.CreateCampaignRequest;
import com.fhict.campaignmanager.mapper.CampaignMapper;
import com.fhict.campaignmanager.mapper.DomainEntityMapper;
import com.fhict.campaignmanager.persistence.entity.CampaignEntity;
import com.fhict.campaignmanager.repository.CampaignRepository;
import com.fhict.campaignmanager.service.CampaignService;
import com.fhict.campaignmanager.service.CampaignAuthorizationService;
import com.fhict.campaignmanager.service.IAuthService;
import com.fhict.campaignmanager.service.IUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock private CampaignRepository campaignRepository;
    @Mock private IAuthService authService;
    @Mock private IUserService userService;
    @Mock private CampaignMapper campaignMapper;
    @Spy private DomainEntityMapper entityMapper = new DomainEntityMapper();

    private CampaignService campaignService;

    @BeforeEach
    void setUp() {
        campaignService = new CampaignService(
                campaignRepository, authService, userService, campaignMapper,
                new CampaignAuthorizationService(), entityMapper);
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
        CampaignEntity savedEntity = entity(saved);
        when(campaignRepository.save(any(CampaignEntity.class))).thenReturn(savedEntity);
        when(campaignMapper.toCampaignResponse(any(Campaign.class))).thenReturn(expected);

        CampaignResponse result = campaignService.createCampaign(request);

        assertEquals(expected, result);
        verify(campaignRepository).save(any(CampaignEntity.class));
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
        when(campaignRepository.findAllByParticipantEntities_User_Id(user.getId()))
                .thenReturn(List.of(entity(first), entity(second)));
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
        when(campaignRepository.findById(10)).thenReturn(Optional.of(entity(campaign)));
        when(userService.getUserById(2)).thenReturn(participant);
        when(campaignRepository.save(any(CampaignEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(campaignMapper.toCampaignResponse(any(Campaign.class))).thenReturn(expected);

        assertEquals(expected, campaignService.updateParticipantRole(10, 2, "AUTHOR"));
        ArgumentCaptor<CampaignEntity> saved = ArgumentCaptor.forClass(CampaignEntity.class);
        verify(campaignRepository).save(saved.capture());
        assertEquals(Role.AUTHOR,
                new DomainEntityMapper().toDomain(saved.getValue()).getParticipants().get(participant));
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
        when(campaignRepository.findById(10)).thenReturn(Optional.of(entity(campaign)));

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
        when(campaignRepository.findById(10)).thenReturn(Optional.of(entity(campaign)));
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
        when(campaignRepository.findById(10)).thenReturn(Optional.of(entity(campaign)));
        when(userService.getUserById(2)).thenReturn(participant);

        campaignService.removeParticipant(10, 2);

        ArgumentCaptor<CampaignEntity> saved = ArgumentCaptor.forClass(CampaignEntity.class);
        verify(campaignRepository).save(saved.capture());
        Campaign savedCampaign = new DomainEntityMapper().toDomain(saved.getValue());
        assertFalse(savedCampaign.getParticipants().containsKey(participant));
        assertEquals(Role.OWNER, savedCampaign.getParticipants().get(owner));
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
        when(campaignRepository.findById(10)).thenReturn(Optional.of(entity(campaign)));

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
        when(campaignRepository.findById(10)).thenReturn(Optional.of(entity(campaign)));
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
        when(campaignRepository.findById(10)).thenReturn(Optional.of(entity(campaign)));
        when(campaignRepository.save(any(CampaignEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(campaignMapper.toCampaignResponse(any(Campaign.class))).thenReturn(expected);

        CampaignResponse result = campaignService.updateCampaign(10,
                com.fhict.campaignmanager.dto.UpdateCampaignRequest.builder()
                        .name("Updated")
                        .description("Updated description")
                        .build());

        assertEquals(expected, result);
        ArgumentCaptor<CampaignEntity> saved = ArgumentCaptor.forClass(CampaignEntity.class);
        verify(campaignRepository).save(saved.capture());
        Campaign savedCampaign = new DomainEntityMapper().toDomain(saved.getValue());
        assertEquals("Updated", savedCampaign.getName());
        assertEquals("Updated description", savedCampaign.getDescription());
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
        when(campaignRepository.findById(10)).thenReturn(Optional.of(entity(campaign)));

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
        when(campaignRepository.findById(10)).thenReturn(Optional.of(entity(campaign)));

        campaignService.deleteCampaign(10);

        verify(campaignRepository).deleteById(campaign.getId());
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
        when(campaignRepository.findById(10)).thenReturn(Optional.of(entity(campaign)));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> campaignService.deleteCampaign(10));

        assertEquals("Only the owner of the campaign can delete it.", exception.getMessage());
        verify(campaignRepository, never()).deleteById(anyInt());
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

    private CampaignEntity entity(Campaign campaign) {
        return new DomainEntityMapper().toEntity(campaign);
    }

    private void setAuthenticatedUser(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, "credentials",
                        List.of(() -> "ROLE_USER")));
    }
}
