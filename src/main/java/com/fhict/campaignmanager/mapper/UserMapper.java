package com.fhict.campaignmanager.mapper;

import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.domain.Role;
import com.fhict.campaignmanager.dto.CreateUserRequest;
import com.fhict.campaignmanager.dto.UserResponse;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class UserMapper
{
    public UserResponse toUserResponse(User user)
    {
        if (user == null)
        {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    public Map<Integer, Role> toUserResponse(Map<User, Role> users)
    {
        if (users == null)
        {
            return null;
        }

        Map<Integer, Role> responses = new LinkedHashMap<>();
        users.forEach((user, role) -> responses.put(user.getId(), role));
        return responses;
    }

    public User toUser(CreateUserRequest createUserRequest)
    {
        if (createUserRequest == null)
        {
            return null;
        }

        return User.builder()
                .username(createUserRequest.getUsername())
                .email(createUserRequest.getEmail())
                .password(createUserRequest.getPassword())
                .build();
    }

    public User toUser(UserResponse userResponse)
    {
        if (userResponse == null)
        {
            return null;
        }

        return User.builder()
                .id(userResponse.getId())
                .username(userResponse.getUsername())
                .email(userResponse.getEmail())
                .build();
    }
}
