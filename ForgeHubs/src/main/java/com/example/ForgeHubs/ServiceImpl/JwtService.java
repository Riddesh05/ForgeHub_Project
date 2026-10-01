package com.example.ForgeHubs.ServiceImpl;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtService {
    private final SecretKey secretKey;
    private final Long expirationMs;
    private final Long refreshExpirationMs;

    public JwtService(@Value("${jwt.secret}")String secretKey,
                      @Value("${jwt.expiration-ms}")Long expirationMs,
                      @Value("${jwt.refresh-expiration-ms}")Long refreshExpirationMs
                      ) {
        this.secretKey= Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateToken(String email,String role) {
        Date now = new Date();

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }
    public String extractSubject(String token) {

        return  Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String generateRefreshToken(String email) {
        Date now = new Date();
        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(new Date (now.getTime()+refreshExpirationMs))
                .signWith(secretKey,Jwts.SIG.HS256)
                .compact();
    }

}
