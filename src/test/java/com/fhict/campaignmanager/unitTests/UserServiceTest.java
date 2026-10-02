package com.fhict.campaignmanager.unitTests;

import com.fhict.campaignmanager.domain.User;
import com.fhict.campaignmanager.dto.CreateUserRequest;
import com.fhict.campaignmanager.dto.UpdateUserRequest;
import com.fhict.campaignmanager.dto.UserResponse;
import com.fhict.campaignmanager.mapper.UserMapper;
import com.fhict.campaignmanager.repository.UserRepository;
import com.fhict.campaignmanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userMapper, userRepository, passwordEncoder);
    }

    @Test
    void createUser_encodesPasswordSavesUserAndReturnsResponse() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("NasiHond")
                .email("test@mail.com")
                .password("12345")
                .build();

        User user = User.builder().password("12345").build();
        User savedUser = User.builder()
                .id(1)
                .username("NasiHond")
                .email("test@mail.com")
                .password("hashed-password")
                .build();
        UserResponse expected = UserResponse.builder()
                .id(1)
                .username("NasiHond")
                .email("test@mail.com")
                .build();

        when(userMapper.toUser(request)).thenReturn(user);
        when(passwordEncoder.encode("12345")).thenReturn("hashed-password");
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toUserResponse(savedUser)).thenReturn(expected);

        UserResponse result = userService.createUser(request);

        assertEquals(expected, result);
        assertEquals("hashed-password", user.getPassword());
        verify(passwordEncoder).encode("12345");
        verify(userRepository).save(user);
    }

    @Test
    void getUser_returnsMappedUserWhenFound() {
        User user = User.builder().id(1).username("NasiHond").build();
        UserResponse expected = UserResponse.builder().id(1).username("NasiHond").build();

        when(userRepository.findById(1)).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(expected);

        assertEquals(expected, userService.getUser(1));
    }

    @Test
    void getUser_returnsNullWhenUserDoesNotExist() {
        when(userRepository.findById(99)).thenReturn(null);

        assertNull(userService.getUser(99));
        verifyNoInteractions(userMapper);
    }

    @Test
    void updateUser_updatesUserAndReturnsMappedResponse() {
        User user = User.builder()
                .id(1)
                .username("old-name")
                .email("old@mail.com")
                .build();
        UpdateUserRequest request = UpdateUserRequest.builder()
                .username("new-name")
                .email("new@mail.com")
                .build();
        UserResponse expected = UserResponse.builder()
                .id(1)
                .username("new-name")
                .email("new@mail.com")
                .build();

        when(userRepository.findById(1)).thenReturn(user);
        when(userRepository.update(user)).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(expected);

        UserResponse result = userService.updateUser(1, request);

        assertEquals(expected, result);
        assertEquals("new-name", user.getUsername());
        assertEquals("new@mail.com", user.getEmail());
        verify(userRepository).update(user);
    }

    @Test
    void deleteUser_delegatesToRepository() {
        userService.deleteUser(1);

        verify(userRepository).delete(1);
    }

    @Test
    void getUserByUsername_delegatesToRepository() {
        User expected = User.builder().username("NasiHond").build();
        when(userRepository.findByUsername("NasiHond")).thenReturn(expected);

        assertEquals(expected, userService.getUserByUsername("NasiHond"));
        verify(userRepository).findByUsername("NasiHond");
    }

    @Test
    void getUserByEmail_delegatesToRepository() {
        User expected = User.builder().email("test@mail.com").build();
        when(userRepository.findByEmail("test@mail.com")).thenReturn(expected);

        assertEquals(expected, userService.getUserByEmail("test@mail.com"));
        verify(userRepository).findByEmail("test@mail.com");
    }

    @Test
    void updateUser_returnsNullWhenUserDoesNotExist() {
        when(userRepository.findById(99)).thenReturn(null);

        assertNull(userService.updateUser(99, UpdateUserRequest.builder()
                .username("new-name")
                .email("new@mail.com")
                .build()));
        verify(userRepository, never()).update(any());
    }
}