//package com.example.ForgeHubs.Entity;
//
//import com.example.ForgeHubs.enums.UserRole;
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Entity
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class User {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long userId;
//    private String name ;
//    private String email;
//    private String password;
//    private UserRole role;
//    private boolean isFirstTimeLogin;
//    private String token;
//    private String refreshToken;
//
//
//
//
//
//}
package com.example.ForgeHubs.Entity;

import com.example.ForgeHubs.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserId")
    private Integer userId;

    @Column(
            name = "FullName",
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private String fullName;

    @Column(
            name = "Email",
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private String email;

    @Column(
            name = "PasswordHash",
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "Role",
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private UserRole role;

    @Column(
            name = "IsFirstTimeLogin",
            nullable = false
    )
    private Boolean isFirstTimeLogin;

    @Column(
            name = "SecretKey",
            columnDefinition = "LONGTEXT"
    )
    private String secretKey;
}
