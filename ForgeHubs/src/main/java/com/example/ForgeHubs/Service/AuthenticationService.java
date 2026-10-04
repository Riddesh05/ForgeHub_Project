package com.example.ForgeHubs.Service;

import com.example.ForgeHubs.DTO.LoginRequestDto;
import com.example.ForgeHubs.DTO.LoginResponseDto;
import com.example.ForgeHubs.DTO.UserRequestDto;
import com.example.ForgeHubs.DTO.UserResponseDto;

public interface AuthenticationService {

    public LoginResponseDto login(LoginRequestDto loginRequestDto);
    public UserResponseDto register(UserRequestDto userRequestDto);
    public void sendRecoveryOtp(String email);

    public LoginResponseDto recoverTwoFactor( String email, String emailOtp, String authenticatorOtp );

    LoginResponseDto refreshToken(String refreshToken);

}
