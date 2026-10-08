package com.fhict.campaignmanager.service;

import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.dto.CreateUserRequest;
import com.fhict.campaignmanager.dto.UpdateUserRequest;
import com.fhict.campaignmanager.dto.UserResponse;

public interface IUserService
{
    UserResponse createUser(CreateUserRequest createUserRequest);

    UserResponse getUser(int id);

    User getUserById(int id);

    User getUserByUsername(String username);

    User getUserByEmail(String email);

    UserResponse updateUser(int id, UpdateUserRequest updateUserRequest);

    void deleteUser(int id);

    boolean checkUsername(String username);
    boolean checkEmail(String email);
    boolean checkPassword(String password);
}
