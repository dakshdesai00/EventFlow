package dev.itsdaksh.controlplane.service;

import dev.itsdaksh.controlplane.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepo userRepo;

    @Override
    public @NonNull UserDetails loadUserByUsername(
            @NonNull String email
    ) throws UsernameNotFoundException {

        return userRepo.findByEmail(email)
                .map(user ->
                        User.builder()
                                .username(user.getEmail())
                                .password(user.getPasswordHash())
                                .build()
                )
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found"
                        ));
    }
}