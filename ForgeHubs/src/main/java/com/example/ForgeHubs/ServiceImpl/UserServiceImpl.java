package com.example.ForgeHubs.ServiceImpl;


import com.example.ForgeHubs.DTO.UserRequestDto;
import com.example.ForgeHubs.DTO.UserResponseDto;
import com.example.ForgeHubs.Entity.User;
import com.example.ForgeHubs.Repository.UserRepository;
import com.example.ForgeHubs.Service.UserService;
import com.example.ForgeHubs.enums.UserRole;
import lombok.AllArgsConstructor;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

  //  private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto addUser(UserRequestDto userRequestDto) {
        return null;
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        return null;
    }

    @Override
    public UserResponseDto getUserByEmail(String email) {
        return null;
    }




    @Override
    @Transactional
    public void createVendor(UserRequestDto request) {

        // Duplicate email check
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException(
                    "User with email already exists: " + request.getEmail()
            );
        }

        User user = new User();




        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // Temporary password must be stored as hash
        user.setPassword(request.getPassword());

        // IMPORTANT:
        // Admin can ONLY create VENDOR
        user.setRole(UserRole.VENDOR);

        // Newly created vendor has first-time login pending
        user.setIsFirstTimeLogin(true);

        // 2FA secret will be generated/configured
        // during first-time authentication flow.
        user.setSecretKey(null);

        userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
