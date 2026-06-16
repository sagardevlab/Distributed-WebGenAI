package com.sagardevlab.distributed_webgenai.account_service.service;


import com.sagardevlab.distributed_webgenai.account_service.dto.auth.AuthResponse;
import com.sagardevlab.distributed_webgenai.account_service.dto.auth.LoginRequest;
import com.sagardevlab.distributed_webgenai.account_service.dto.auth.SignupRequest;

public interface AuthService {
    AuthResponse signup(SignupRequest request);

    AuthResponse login(LoginRequest request);
}
