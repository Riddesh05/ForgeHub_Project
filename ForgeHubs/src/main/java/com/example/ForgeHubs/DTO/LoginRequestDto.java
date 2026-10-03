package com.example.ForgeHubs.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginRequestDto {

    private String email;
    private String password;
    private String authenticatorOtp;
    private String emailOtp;
}
