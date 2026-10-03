package com.example.ForgeHubs.Service;


import com.example.ForgeHubs.DTO.UserCreateRequest;
import com.example.ForgeHubs.DTO.UserRequestDto;
import com.example.ForgeHubs.DTO.UserResponseDto;
import com.example.ForgeHubs.Entity.User;

import java.util.List;

public interface UserService {

    public UserResponseDto addUser(UserRequestDto userRequestDto);
    public UserResponseDto getUserById(Long id);
    public UserResponseDto getUserByEmail(String email);
    void createVendor(UserCreateRequest request);

    List<User> getAllUsers();
}
