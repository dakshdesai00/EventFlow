package dev.itsdaksh.controlplane.security;

import dev.itsdaksh.controlplane.service.CustomUserDetailsService;
import dev.itsdaksh.controlplane.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader =
                request.getHeader(
                        HttpHeaders.AUTHORIZATION
                );
        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(
                    request,
                    response
            );
            return;
        }
        String token =
                authHeader.substring(7);
        try {
            if (!jwtService.isTokenValid(token)) {
                response.sendError(
                        HttpStatus.UNAUTHORIZED.value(),
                        "Invalid JWT token"
                );
                return;
            }
            if (SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {
                String email =
                        jwtService.extractEmail(token);
                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );
                SecurityContextHolder
                        .getContext()
                        .setAuthentication(
                                authentication
                        );
            }
            filterChain.doFilter(
                    request,
                    response
            );
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            response.sendError(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Invalid JWT token"
            );
        }
    }

}