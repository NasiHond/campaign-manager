package com.fhict.campaignmanager.service;

import com.fhict.campaignmanager.dto.LoginRequest;
import com.fhict.campaignmanager.dto.LoginResponse;

public interface IAuthService
{
    LoginResponse login(LoginRequest loginRequest);
}
