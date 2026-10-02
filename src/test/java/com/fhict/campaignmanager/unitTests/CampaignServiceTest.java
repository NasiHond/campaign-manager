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

    private void setAuthenticatedUser(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, "credentials",
                        List.of(() -> "ROLE_USER")));
    }
}
