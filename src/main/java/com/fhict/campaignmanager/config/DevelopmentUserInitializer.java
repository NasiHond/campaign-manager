package com.fhict.campaignmanager.config;

import com.fhict.campaignmanager.dto.CreateUserRequest;
import com.fhict.campaignmanager.service.IUserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DevelopmentUserInitializer implements CommandLineRunner {

    private static final String EMAIL = "test@test.com";
    private static final String PASSWORD = "12345678";

    private final IUserService userService;

    public DevelopmentUserInitializer(IUserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        createIfMissing("test");
        createIfMissing("test2");
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
}
