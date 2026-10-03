//package com.example.ForgeHubs.DTO;
//
//import jakarta.validation.constraints.Email;
//import jakarta.validation.constraints.NotBlank;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//public class UserCreateRequest {
//
//    @NotBlank(message = "Full name is required")
//    private String fullName;
//
//    @NotBlank(message = "Email is required")
//    @Email(message = "Enter a valid email address")
//    private String email;
//
//    @NotBlank(message = "Temporary password is required")
//    private String temporaryPassword;
//}