package com.example.ForgeHubs.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponseDto {

    private boolean requiresTwoFactorSetup;
    private boolean requiresTwoFactor;
    private String qrCode;
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String role;
    private String name;
    private String email;
}
