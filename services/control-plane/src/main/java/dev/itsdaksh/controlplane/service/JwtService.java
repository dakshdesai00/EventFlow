package dev.itsdaksh.controlplane.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET =
            "njQth1FNg7efUZFilSVzWmCX20miUPc8";

    private final SecretKey key =
            Keys.hmacShaKeyFor(
                    SECRET.getBytes(StandardCharsets.UTF_8)
            );

    public String generateToken(
            Long userId,
            String email
    ) {

        return Jwts.builder()
                .subject(email)
                .claim("uid", userId)
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + 1000L * 60 * 60 * 24 * 7
                        )
                )
                .signWith(key)
                .compact();
    }

    public Long extractUserId(
            String token
    ) {

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("uid", Long.class);
    }

    public String extractEmail(
            String token
    ) {

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isTokenValid(
            String token
    ) {

        try {

            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (Exception e) {
            return false;
        }
    }
}