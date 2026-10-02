package com.fhict.campaignmanager.controller;

import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.dto.CreateUserRequest;
import com.fhict.campaignmanager.dto.LoginResponse;
import com.fhict.campaignmanager.dto.UpdateUserRequest;
import com.fhict.campaignmanager.dto.UserResponse;
import com.fhict.campaignmanager.service.UserService;
import com.fhict.campaignmanager.service.IAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final IAuthService authService;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService, IAuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @CrossOrigin
    @PostMapping
    public LoginResponse createUser(@RequestBody CreateUserRequest createUserRequest)
    {
        LOGGER.info("Received request to create user: {}", createUserRequest);
        return authService.register(createUserRequest);
    }

    @CrossOrigin
    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable int id)
    {
        LOGGER.info("Received request to get user with id: {}", id);
        return userService.getUser(id);
    }

    @CrossOrigin
    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable int id, @RequestBody UpdateUserRequest updateUserRequest)
    {
        LOGGER.info("Received request to update user with id: {}", id);
        return userService.updateUser(id, updateUserRequest);
    }

    @CrossOrigin
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable int id)
    {
        LOGGER.info("Received request to delete user with id: {}", id);
        userService.deleteUser(id);
    }
}
