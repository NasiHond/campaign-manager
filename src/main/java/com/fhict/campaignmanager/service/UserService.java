package com.fhict.campaignmanager.service;

import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.dto.CreateUserRequest;
import com.fhict.campaignmanager.dto.UpdateUserRequest;
import com.fhict.campaignmanager.dto.UserResponse;
import com.fhict.campaignmanager.mapper.UserMapper;
import com.fhict.campaignmanager.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService
{
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        User user = userMapper.toUser(createUserRequest);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);
        return userMapper.toUserResponse(savedUser);
    }

    @Override
    public UserResponse getUser(int id) {
        User user = userRepository.findById(id);
        if (user != null) {
            return userMapper.toUserResponse(user);
        }
        return null;
    }

    @Override
    public User getUserById(int id) {
        return userRepository.findById(id);
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserResponse updateUser(int id, UpdateUserRequest updateUserRequest) {
        User user = userRepository.findById(id);
        if (user != null)
        {
            user.setUsername(updateUserRequest.getUsername());
            user.setEmail(updateUserRequest.getEmail());
            User updatedUser = userRepository.update(user);
            return userMapper.toUserResponse(updatedUser);
        }
        return null;
    }

    @Override
    public void deleteUser(int id) {
        userRepository.delete(id);
    }

    @Override
    public boolean checkUsername(String username) {

        return false;
    }

    @Override
    public boolean checkEmail(String email) {
        return false;
    }

    @Override
    public boolean checkPassword(String password) {
        return false;
    }


}
