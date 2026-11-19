package com.fhict.campaignmanager.controller;

import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.dto.CreateUserRequest;
import com.fhict.campaignmanager.dto.LoginResponse;
import com.fhict.campaignmanager.dto.UpdateUserRequest;
import com.fhict.campaignmanager.dto.UserResponse;
import com.fhict.campaignmanager.mapper.AuthMapper;
import com.fhict.campaignmanager.mapper.UserMapper;
import com.fhict.campaignmanager.service.UserService;
import com.fhict.campaignmanager.service.IAuthService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final AuthMapper authMapper;
    private final IAuthService authService;

    public UserController(UserService userService, UserMapper userMapper, AuthMapper authMapper, IAuthService authService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.authMapper = authMapper;
        this.authService = authService;
    }

    @PostMapping
    public LoginResponse createUser(@RequestBody CreateUserRequest createUserRequest)
    {
        User createdUser = userService.createUser(createUserRequest);
        String token = authService.createToken(createdUser);
        return authMapper.toLoginResponse(
                token,
                3600,
                "Bearer",
                userMapper.toUserResponse(createdUser)
        );
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable int id)
    {
        return userMapper.toUserResponse(userService.getUser(id));
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable int id, @RequestBody UpdateUserRequest updateUserRequest)
    {
        return userMapper.toUserResponse(userService.updateUser(id, updateUserRequest));
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable int id)
    {
        userService.deleteUser(id);
    }
}
