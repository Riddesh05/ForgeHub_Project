package com.example.ForgeHubs.Controller;

import com.example.ForgeHubs.DTO.LoginRequestDto;
import com.example.ForgeHubs.DTO.LoginResponseDto;
import com.example.ForgeHubs.DTO.UserRequestDto;
import com.example.ForgeHubs.DTO.UserResponseDto;
import com.example.ForgeHubs.Service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public UserResponseDto register(@RequestBody UserRequestDto requestDto) {
        return authenticationService.register(requestDto);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto requestDto) {
        return withAccessCookie(authenticationService.login(requestDto));
    }

    @PostMapping("/2fa/recovery/send-otp")
    public String sendRecoveryOtp(@RequestParam String email) {
        authenticationService.sendRecoveryOtp(email);
        return "Recovery OTP sent successfully";
    }

    @PostMapping("/2fa/recovery")
    public ResponseEntity<LoginResponseDto> recoverTwoFactor(@RequestBody LoginRequestDto requestDto) {
        return withAccessCookie(authenticationService.recoverTwoFactor(
                requestDto.getEmail(),
                requestDto.getEmailOtp(),
                requestDto.getAuthenticatorOtp()
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refreshToken(@RequestBody String refreshToken) {
        return withAccessCookie(authenticationService.refreshToken(refreshToken));
    }

    private ResponseEntity<LoginResponseDto> withAccessCookie(LoginResponseDto response) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();

        if (response.getAccessToken() != null && !response.getAccessToken().isBlank()) {
            ResponseCookie cookie = ResponseCookie.from(
                            "FORGEHUB_ACCESS_TOKEN",
                            response.getAccessToken()
                    )
                    .httpOnly(true)
                    .secure(false)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(Duration.ofMinutes(15))
                    .build();

            builder.header(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        return builder.body(response);
    }
}
