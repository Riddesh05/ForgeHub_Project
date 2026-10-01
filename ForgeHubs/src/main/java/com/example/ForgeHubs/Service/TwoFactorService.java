package com.example.ForgeHubs.Service;

public interface TwoFactorService {

    String generateSecret();

    String generateQrCodeUri(String email, String secret);

    boolean verifyCode(String secret, String code);
}