package com.example.ForgeHubs.DTO;


import com.example.ForgeHubs.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    private Long userId;
    private String name ;
    private String email;
    private UserRole role;
    private boolean isFirstTimeLogin;
    private String token;
    private String refreshToken;


}
