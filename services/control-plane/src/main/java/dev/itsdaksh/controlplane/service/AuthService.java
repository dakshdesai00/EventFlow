package dev.itsdaksh.controlplane.service;

import dev.itsdaksh.controlplane.dto.AuthRequests.AuthResponse;
import dev.itsdaksh.controlplane.dto.AuthRequests.LoginRequest;
import dev.itsdaksh.controlplane.dto.AuthRequests.RegisterRequest;
import dev.itsdaksh.controlplane.entity.User;
import dev.itsdaksh.controlplane.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepo;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    public Optional<AuthResponse> register(
            RegisterRequest request
    ) {

        if (userRepo.existsByEmail(request.email())) {
            return Optional.empty();
        }

        User user =
                User.builder()
                        .email(request.email())
                        .passwordHash(
                                passwordEncoder.encode(
                                        request.password()
                                )
                        )
                        .build();

        user = userRepo.save(user);

        String token =
                jwtService.generateToken(
                        user.getId(),
                        user.getEmail()
                );

        return Optional.of(
                new AuthResponse(token)
        );
    }

    public Optional<AuthResponse> login(
            LoginRequest request
    ) {

        return userRepo.findByEmail(
                        request.email()
                )
                .filter(user ->
                        passwordEncoder.matches(
                                request.password(),
                                user.getPasswordHash()
                        )
                )
                .map(user ->
                        new AuthResponse(
                                jwtService.generateToken(
                                        user.getId(),
                                        user.getEmail()
                                )
                        )
                );
    }
}