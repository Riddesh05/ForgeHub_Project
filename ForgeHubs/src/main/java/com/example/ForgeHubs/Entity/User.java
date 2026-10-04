package com.example.ForgeHubs.Entity;

import com.example.ForgeHubs.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long userId;

    private String name;

    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private boolean isFirstTimeLogin;

    private String token;

    private String refreshToken;

    private boolean twoFactorEnabled;

    private String twoFactorSecret;

    private String pendingTwoFactorSecret;

    private String recoveryOtp;

    private LocalDateTime recoveryOtpExpiry;





}
