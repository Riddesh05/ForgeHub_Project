package com.example.ForgeHubs.Service;

import com.example.ForgeHubs.DTO.UserRequestDto;
import com.example.ForgeHubs.DTO.UserResponseDto;

public interface AuthenticationService {

    public UserResponseDto login();
    public UserResponseDto register(UserRequestDto userRequestDto);
}
