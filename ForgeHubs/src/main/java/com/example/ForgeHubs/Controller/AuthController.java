package com.example.ForgeHubs.Controller;

import com.example.ForgeHubs.DTO.LoginRequestDto;
import com.example.ForgeHubs.DTO.LoginResponseDto;
import com.example.ForgeHubs.DTO.UserRequestDto;
import com.example.ForgeHubs.DTO.UserResponseDto;
import com.example.ForgeHubs.Service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public UserResponseDto register( @RequestBody UserRequestDto requestDto) {
        return authenticationService.register(requestDto);
    }

    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody LoginRequestDto requestDto) {
        return authenticationService.login(requestDto);
    }

    @PostMapping("/2fa/recovery/send-otp")
    public String sendRecoveryOtp( @RequestParam String email) {
        authenticationService.sendRecoveryOtp(email);
        return "Recovery OTP sent successfully";
    }

    @PostMapping("/2fa/recovery")
    public LoginResponseDto recoverTwoFactor( @RequestBody LoginRequestDto requestDto) {
        return authenticationService.recoverTwoFactor(
                requestDto.getEmail(),
                requestDto.getEmailOtp(),
                requestDto.getAuthenticatorOtp()
        );
    }

    @PostMapping("/refresh")
    public LoginResponseDto refreshToken(
            @RequestBody String refreshToken
    ) {
        return authenticationService.refreshToken(refreshToken);
    }

}
