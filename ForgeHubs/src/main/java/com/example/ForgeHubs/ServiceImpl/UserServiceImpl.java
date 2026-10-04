package com.example.ForgeHubs.ServiceImpl;


import com.example.ForgeHubs.DTO.UserRequestDto;
import com.example.ForgeHubs.DTO.UserResponseDto;
import com.example.ForgeHubs.Entity.User;
import com.example.ForgeHubs.Exception.BusinessException;
import com.example.ForgeHubs.Repository.UserRepository;
import com.example.ForgeHubs.Service.UserService;
import com.example.ForgeHubs.enums.UserRole;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

  //  private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponseDto addUser(UserRequestDto request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("A user with this email already exists. Please use a different email address.");
        }

        User user = modelMapper.map(request, User.class);
        user.setRole(request.getRole() == null ? UserRole.VENDOR : request.getRole());
        user.setIsFirstTimeLogin(true);
        user.setSecretKey(null);

        return modelMapper.map(userRepository.save(user), UserResponseDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(user -> modelMapper.map(user, UserResponseDto.class))
                .orElseThrow(() -> new BusinessException("User not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> modelMapper.map(user, UserResponseDto.class))
                .orElseThrow(() -> new BusinessException("User not found with email: " + email));
    }

    @Override
    @Transactional
    public UserResponseDto createVendor(UserRequestDto request) {

        // Duplicate email check
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(
                    "A user with this email already exists. Please use a different email address."
            );
        }

        User user = modelMapper.map(request, User.class);

        // Temporary password must be stored as hash

        // IMPORTANT:
        // Admin can ONLY create VENDOR
        user.setRole(UserRole.VENDOR);

        // Newly created vendor has first-time login pending
        user.setIsFirstTimeLogin(true);

        // 2FA secret will be generated/configured
        // during first-time authentication flow.
        user.setSecretKey(null);

        return modelMapper.map(userRepository.save(user), UserResponseDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, UserResponseDto.class))
                .toList();
    }
}
