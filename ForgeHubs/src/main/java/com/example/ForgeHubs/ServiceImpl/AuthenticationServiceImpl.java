package com.example.ForgeHubs.ServiceImpl;

import com.example.ForgeHubs.DTO.LoginRequestDto;
import com.example.ForgeHubs.DTO.LoginResponseDto;
import com.example.ForgeHubs.DTO.UserRequestDto;
import com.example.ForgeHubs.DTO.UserResponseDto;
import com.example.ForgeHubs.Entity.User;
import com.example.ForgeHubs.Repository.UserRepo;
import com.example.ForgeHubs.Service.AuthenticationService;
import com.example.ForgeHubs.Service.EmailService;
import com.example.ForgeHubs.Service.TwoFactorService;
import com.example.ForgeHubs.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TwoFactorService  twoFactorService;
    private final ModelMapper mapper;
    private final EmailService emailService;


    @Override
    public LoginResponseDto login(LoginRequestDto requestDto) {
        User user = userRepo.findByEmail(requestDto.getEmail());

        System.out.println("LOGIN EMAIL = [" + requestDto.getEmail() + "]");
        if (user == null) {
            throw new AuthenticationException("Invalid email");
        }
        System.out.println("USER FOUND = " + user);
        if (requestDto.getAuthenticatorOtp() == null ||
                requestDto.getAuthenticatorOtp().isBlank()) {

            if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {

                throw new AuthenticationException("Invalid password");
            }
        }

        // Complete a pending first-time/recovery 2FA setup when an OTP is supplied.
        // This keeps the existing recovery flow working without storing passwords in the browser.
        if (requestDto.getAuthenticatorOtp() != null
                && !requestDto.getAuthenticatorOtp().isBlank()
                && user.getPendingTwoFactorSecret() != null) {

            boolean valid = twoFactorService.verifyCode(
                    user.getPendingTwoFactorSecret(),
                    requestDto.getAuthenticatorOtp()
            );

            if (!valid) {
                throw new AuthenticationException("Invalid OTP");
            }

            user.setTwoFactorSecret(user.getPendingTwoFactorSecret());
            user.setPendingTwoFactorSecret(null);
            user.setTwoFactorEnabled(true);
            user.setFirstTimeLogin(false);
            user.setRecoveryOtp(null);
            user.setRecoveryOtpExpiry(null);
            userRepo.save(user);

            String accessToken = jwtService.generateToken(
                    user.getEmail(),
                    user.getRole().name()
            );
            String refreshToken = jwtService.generateRefreshToken(user.getEmail());

            return LoginResponseDto.builder()
                    .requiresTwoFactorSetup(false)
                    .requiresTwoFactor(false)
                    .qrCode(null)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .userId(user.getUserId())
                    .role(user.getRole().name())
                    .build();
        }

        if (!user.isTwoFactorEnabled()) {

            if (requestDto.getAuthenticatorOtp() == null ||
                    requestDto.getAuthenticatorOtp().isBlank()) {

                String secret = twoFactorService.generateSecret();

                user.setPendingTwoFactorSecret(secret);
                userRepo.save(user);

                String qrCode = twoFactorService.generateQrCode(
                        user.getEmail(),
                        secret
                );

                return LoginResponseDto.builder()
                        .requiresTwoFactorSetup(true)
                        .requiresTwoFactor(false)
                        .qrCode(qrCode)
                        .accessToken(null)
                        .refreshToken(null)
                        .userId(user.getUserId())
                        .role(user.getRole().name())
                        .name(user.getName())
                        .email(user.getEmail())
                        .build();
            }

            if (user.getPendingTwoFactorSecret() == null) {
                throw new AuthenticationException(
                        "Two-factor setup is not initialized"
                );
            }

            boolean valid = twoFactorService.verifyCode(
                    user.getPendingTwoFactorSecret(),
                    requestDto.getAuthenticatorOtp()
            );

            if (!valid) {
                throw new AuthenticationException("Invalid OTP");
            }

            user.setTwoFactorSecret(user.getPendingTwoFactorSecret());
            user.setPendingTwoFactorSecret(null);
            user.setTwoFactorEnabled(true);
            user.setFirstTimeLogin(false);

            userRepo.save(user);

            String accessToken = jwtService.generateToken(
                    user.getEmail(),
                    user.getRole().name()
            );

            String refreshToken = jwtService.generateRefreshToken(
                    user.getEmail()
            );

            return LoginResponseDto.builder()
                    .requiresTwoFactorSetup(false)
                    .requiresTwoFactor(false)
                    .qrCode(null)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .userId(user.getUserId())
                    .role(user.getRole().name())
                    .build();
        }
      if (requestDto.getAuthenticatorOtp()==null || requestDto.getAuthenticatorOtp().isBlank()) {
          return LoginResponseDto.builder()
                  .requiresTwoFactorSetup(false)
                  .requiresTwoFactor(true)
                  .qrCode(null)
                  .accessToken(null)
                  .refreshToken(null)
                  .userId(user.getUserId())
                  .role(user.getRole().name())
                  .build();
      }
      boolean valid = twoFactorService.verifyCode( user.getTwoFactorSecret(), requestDto.getAuthenticatorOtp());
      if (!valid) {
          throw new AuthenticationException("Invalid OTP");
      }

      String accessToken = jwtService.generateToken(user.getEmail(), user.getRole().name());
      String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return LoginResponseDto.builder()
                .requiresTwoFactorSetup(false)
                .requiresTwoFactor(false)
                .qrCode(null)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getUserId())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public UserResponseDto register(UserRequestDto requestDto) {
        User existingUser = userRepo.findByEmail(requestDto.getEmail());

        if (existingUser != null) {
            throw new AuthenticationException("Email already registered");
        }

        User user = User.builder()
                .name(requestDto.getName())
                .email(requestDto.getEmail())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .role(requestDto.getRole())
                .isFirstTimeLogin(true)
                .twoFactorEnabled(false)
                .twoFactorSecret(null)
                .pendingTwoFactorSecret(null)
                .recoveryOtp(null)
                .recoveryOtpExpiry(null)
                .build();

        User savedUser = userRepo.save(user);

        return mapper.map(savedUser, UserResponseDto.class);
    }

    @Override
    public void sendRecoveryOtp(String email) {
        User user = userRepo.findByEmail(email);

        if (user == null) {
            throw new AuthenticationException("User not found");
        }

        if (!user.isTwoFactorEnabled()) {
            throw new AuthenticationException("Two-factor authentication is not enabled");
        }

        String otp = String.valueOf(
                ThreadLocalRandom.current().nextInt(100000, 1000000)
        );

        user.setRecoveryOtp(otp);
        user.setRecoveryOtpExpiry(LocalDateTime.now().plusMinutes(5));

        userRepo.save(user);

        emailService.sendOtp(email, otp);
    }

    @Override
    public LoginResponseDto recoverTwoFactor(String email, String emailOtp, String authenticatorOtp) {
        User user = userRepo.findByEmail(email);


        if (user == null) {
            throw new AuthenticationException("Invalid recovery request");
        }

        if (!user.isTwoFactorEnabled()) {
            throw new AuthenticationException("Two-factor authentication is not enabled");
        }

        if (emailOtp != null && !emailOtp.isBlank()) {

            if (user.getRecoveryOtp() == null ||
                    user.getRecoveryOtpExpiry() == null) {
                throw new AuthenticationException("Recovery OTP not found");
            }

            if (LocalDateTime.now().isAfter(user.getRecoveryOtpExpiry())) {

                user.setRecoveryOtp(null);
                user.setRecoveryOtpExpiry(null);
                userRepo.save(user);

                throw new AuthenticationException("Recovery OTP expired");
            }

            if (!emailOtp.equals(user.getRecoveryOtp())) {
                throw new AuthenticationException("Invalid recovery OTP");
            }

            user.setRecoveryOtp(null);
            user.setRecoveryOtpExpiry(null);

            String newSecret = twoFactorService.generateSecret();

            user.setPendingTwoFactorSecret(newSecret);

            userRepo.save(user);

            String qrCode = twoFactorService.generateQrCode(
                    user.getEmail(),
                    newSecret
            );

            return LoginResponseDto.builder()
                    .requiresTwoFactorSetup(true)
                    .requiresTwoFactor(false)
                    .qrCode(qrCode)
                    .accessToken(null)
                    .refreshToken(null)
                    .userId(user.getUserId())
                    .role(user.getRole().name())
                    .build();
        }

        if (authenticatorOtp != null && !authenticatorOtp.isBlank()) {

            if (user.getPendingTwoFactorSecret() == null) {
                throw new AuthenticationException(
                        "Two-factor recovery setup not started"
                );
            }

            boolean valid = twoFactorService.verifyCode(
                    user.getPendingTwoFactorSecret(),
                    authenticatorOtp
            );

            if (!valid) {
                throw new AuthenticationException("Invalid authenticator OTP");
            }

            user.setTwoFactorSecret(user.getPendingTwoFactorSecret());
            user.setPendingTwoFactorSecret(null);
            user.setTwoFactorEnabled(true);
            user.setRecoveryOtp(null);
            user.setRecoveryOtpExpiry(null);

            userRepo.save(user);

            String accessToken = jwtService.generateToken( user.getEmail(), user.getRole().name());

            String refreshToken = jwtService.generateRefreshToken( user.getEmail());

            return LoginResponseDto.builder()
                    .requiresTwoFactorSetup(false)
                    .requiresTwoFactor(false)
                    .qrCode(null)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .userId(user.getUserId())
                    .role(user.getRole().name())
                    .build();
        }

        throw new AuthenticationException("Recovery verification required");
    }

    @Override
    public LoginResponseDto refreshToken(String refreshToken) {

        try {

            String type = jwtService.extractTokenType(refreshToken);

            if (!"refresh".equals(type)) {
                throw new AuthenticationException("Invalid refresh token");
            }

            String email = jwtService.extractSubject(refreshToken);

            User user = userRepo.findByEmail(email);

            if (user == null) {
                throw new AuthenticationException("User not found");
            }

            String accessToken = jwtService.generateToken(
                    user.getEmail(),
                    user.getRole().name()
            );

            return LoginResponseDto.builder()
                    .requiresTwoFactorSetup(false)
                    .requiresTwoFactor(false)
                    .qrCode(null)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .userId(user.getUserId())
                    .role(user.getRole().name())
                    .build();

        } catch (Exception e) {
            throw new AuthenticationException(
                    "Invalid or expired refresh token"
            );
        }
    }
}
