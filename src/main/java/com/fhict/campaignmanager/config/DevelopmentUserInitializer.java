package com.fhict.campaignmanager.config;

import com.fhict.campaignmanager.domain.Campaign;
import com.fhict.campaignmanager.domain.Role;
import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.dto.CreateUserRequest;
import com.fhict.campaignmanager.repository.CampaignRepository;
import com.fhict.campaignmanager.service.IUserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DevelopmentUserInitializer implements CommandLineRunner {

    private static final String EMAIL = "test@test.com";
    private static final String PASSWORD = "12345678";

    private final IUserService userService;
    private final CampaignRepository campaignRepository;

    public DevelopmentUserInitializer(IUserService userService, CampaignRepository campaignRepository) {
        this.userService = userService;
        this.campaignRepository = campaignRepository;
    }

    @Override
    public void run(String... args) {
        createIfMissing("test");
        createIfMissing("test2");
        createTestCampaignIfMissing();
    }

    private void createIfMissing(String username) {
        if (userService.getUserByUsername(username) == null) {
            userService.createUser(CreateUserRequest.builder()
                    .username(username)
                    .email(EMAIL)
                    .password(PASSWORD)
                    .build());
        }
    }

    private void createTestCampaignIfMissing() {
        User owner = userService.getUserByUsername("test");
        boolean campaignExists = owner != null
                && campaignRepository.findAllByParticipantsContainingKey(owner).stream()
                .anyMatch(campaign -> "test".equals(campaign.getName())
                        && "test".equals(campaign.getDescription()));
        if (!campaignExists) {
            campaignRepository.save(Campaign.builder()
                    .name("test")
                    .description("test")
                    .participants(new HashMap<>(Map.of(owner, Role.OWNER)))
                    .build());
        }
    }
}
