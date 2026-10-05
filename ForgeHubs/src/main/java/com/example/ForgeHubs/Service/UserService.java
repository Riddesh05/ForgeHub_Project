package com.example.ForgeHubs.Service;


import com.example.ForgeHubs.DTO.UserRequestDto;
import com.example.ForgeHubs.DTO.UserResponseDto;

import java.util.List;

public interface UserService {

    public UserResponseDto addUser(UserRequestDto userRequestDto);
    public UserResponseDto getUserById(Long id);
    public UserResponseDto getUserByEmail(String email);


    UserResponseDto createVendor(UserRequestDto request);

    List<UserResponseDto> getAllUsers();
}
