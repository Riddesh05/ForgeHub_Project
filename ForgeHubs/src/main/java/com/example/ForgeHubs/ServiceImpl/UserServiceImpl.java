package com.example.ForgeHubs.ServiceImpl;


import com.example.ForgeHubs.DTO.UserRequestDto;
import com.example.ForgeHubs.DTO.UserResponseDto;
import com.example.ForgeHubs.Entity.User;
import com.example.ForgeHubs.Repository.UserRepository;
import com.example.ForgeHubs.Service.UserService;
import com.example.ForgeHubs.enums.UserRole;
import com.example.ForgeHubs.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final ModelMapper mapper;
    private final UserRepository userRepository;

  //  private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto addUser(UserRequestDto userRequestDto) {
        User user = User.builder()
                .name(userRequestDto.getName())
                .email(userRequestDto.getEmail())
                .password(passwordEncoder.encode(userRequestDto.getPassword()))
                .role(userRequestDto.getRole())
                .isFirstTimeLogin(true)
                .twoFactorEnabled(false)
                .build();
        User saved  = userRepository.save(user);
        return  mapper.map(saved,UserResponseDto.class);
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        if(userRepository.findById(id).isPresent()){
            return mapper.map(userRepository.findById(id).get(),UserResponseDto.class);

        }
        return null;
    }

    @Override
    public UserResponseDto getUserByEmail(String email) {
        if(userRepository.findByEmail(email)!=null){
            return mapper.map(userRepository.findByEmail(email),UserResponseDto.class);
        }
        return null;
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

        User user = mapper.map(request, User.class);


        System.out.println("RAW PASSWORD: " + request.getPassword());

        System.out.println(
                "ENCODED PASSWORD: " +
                        passwordEncoder.encode(request.getPassword())
        );

        // IMPORTANT: Store password as BCrypt hash
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        // Admin can ONLY create VENDOR
        user.setRole(UserRole.VENDOR);

        // Newly created vendor has first-time login pending
        user.setFirstTimeLogin(true);

        // 2FA secret will be generated/configured
        // during first-time authentication flow.
        user.setSecretKey(null);

        return mapper.map(
                userRepository.save(user),
                UserResponseDto.class
        );
    }
    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> mapper.map(user, UserResponseDto.class))
                .toList();
    }
}
