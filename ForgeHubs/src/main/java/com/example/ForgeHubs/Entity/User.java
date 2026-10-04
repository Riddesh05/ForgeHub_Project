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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(
            name = "full_name",
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private String name;

    @Column(
            name = "email",
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private String email;

    @Column(
            name = "password_hash",
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "role",
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private UserRole role;

    @Column(
            name = "is_first_time_login",
            nullable = false
    )
    private Boolean isFirstTimeLogin;

    @Column(
            name = "secret_key",
            columnDefinition = "LONGTEXT"
    )
    private String secretKey;
}
