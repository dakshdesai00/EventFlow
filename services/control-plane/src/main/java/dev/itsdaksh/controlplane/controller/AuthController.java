package dev.itsdaksh.controlplane.controller;

import dev.itsdaksh.controlplane.dto.AuthRequests.LoginRequest;
import dev.itsdaksh.controlplane.dto.AuthRequests.RegisterRequest;
import dev.itsdaksh.controlplane.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid
            @RequestBody
            RegisterRequest request
    ) {

        return authService
                .register(request)
                .<ResponseEntity<?>>map(response ->
                        ResponseEntity.status(
                                HttpStatus.CREATED
                        ).body(response)
                )
                .orElseGet(() ->
                        ResponseEntity.status(
                                HttpStatus.CONFLICT
                        ).body(
                                "Email already exists"
                        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid
            @RequestBody
            LoginRequest request
    ) {

        return authService
                .login(request)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.status(
                                HttpStatus.UNAUTHORIZED
                        ).body(
                                "Invalid credentials"
                        ));
    }
}