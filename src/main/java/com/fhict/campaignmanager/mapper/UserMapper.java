package com.fhict.campaignmanager.mapper;

import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.dto.CreateUserRequest;
import com.fhict.campaignmanager.dto.UserResponse;
import org.springframework.stereotype.Service;

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
