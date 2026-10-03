package com.example.ForgeHubs.ServiceImpl;


import com.example.ForgeHubs.DTO.UserRequestDto;
import com.example.ForgeHubs.DTO.UserResponseDto;
import com.example.ForgeHubs.Service.UserService;

public class UserServiceImpl implements UserService {
    @Override
    public UserResponseDto addUser(UserRequestDto userRequestDto) {
        User user = User.builder()
                .name(userRequestDto.getName())
                .email(userRequestDto.getEmail())
                .password(encoder.encode(userRequestDto.getPassword()))
                .role(userRequestDto.getRole())
                .isFirstTimeLogin(true)
                .twoFactorEnabled(false)
                .build();
        User saved  = repo.save(user);
        return  mapper.map(saved,UserResponseDto.class);
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        if(repo.findById(id).isPresent()){
            return mapper.map(repo.findById(id).get(),UserResponseDto.class);

        }
        return null;
    }

    @Override
    public UserResponseDto getUserByEmail(String email) {
        if(repo.findByEmail(email)!=null){
            return mapper.map(repo.findByEmail(email),UserResponseDto.class);
        }
        return null;
    }
}
