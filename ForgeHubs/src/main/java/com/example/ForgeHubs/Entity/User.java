package com.example.ForgeHubs.Entity;

import com.example.ForgeHubs.enums.UserRole;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    private String name ;
    private String email;
    private String password;
    private UserRole role;
    private boolean isFirstTimeLogin;
    private boolean twoFactorEnabled;
    private String twoFactorSecret;
    private String pendingTwoFactorSecret;
    private String token;
    private String refreshToken;





}
