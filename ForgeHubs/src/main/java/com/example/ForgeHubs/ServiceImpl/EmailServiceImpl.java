package com.example.ForgeHubs.ServiceImpl;

import com.example.ForgeHubs.Service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendOtp(String email, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("ForgeHubs - 2FA Recovery OTP");
        message.setText(
                "Your ForgeHubs 2FA recovery OTP is: " + otp +
                "\n\nThis OTP is valid for 5 minutes."
        );

        mailSender.send(message);
    }
}