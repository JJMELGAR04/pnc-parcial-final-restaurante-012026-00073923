package com.uca.pncparcialfinalrestaurante.service;

import com.uca.pncparcialfinalrestaurante.domain.RefreshToken;
import com.uca.pncparcialfinalrestaurante.domain.Usuario;
import com.uca.pncparcialfinalrestaurante.dto.request.LoginRequest;
import com.uca.pncparcialfinalrestaurante.dto.request.RefreshTokenRequest;
import com.uca.pncparcialfinalrestaurante.dto.response.LoginResponse;
import com.uca.pncparcialfinalrestaurante.exception.InvalidCredentialsException;
import com.uca.pncparcialfinalrestaurante.exception.InvalidRefreshTokenException;
import com.uca.pncparcialfinalrestaurante.repository.RefreshTokenRepository;
import com.uca.pncparcialfinalrestaurante.repository.UsuarioRepository;
import com.uca.pncparcialfinalrestaurante.security.CustomUserDetails;
import com.uca.pncparcialfinalrestaurante.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        CustomUserDetails userDetails;

        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
            userDetails = (CustomUserDetails) authentication.getPrincipal();
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException("Email o contrasena invalidos");
        }

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshTokenValue = jwtService.generateRefreshTokenValue(userDetails);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .usuario(userDetails.getUsuario())
                .fechaExpiracion(LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpirationMs() / 1000))
                .revocado(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return LoginResponse.of(accessToken, refreshTokenValue, jwtService.getAccessTokenExpirationMs());
    }

    @Transactional
    public LoginResponse refresh(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token invalido"));

        if (storedToken.isRevocado()) {
            throw new InvalidRefreshTokenException("Refresh token revocado");
        }

        if (storedToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new InvalidRefreshTokenException("Refresh token expirado");
        }

        Usuario usuario = storedToken.getUsuario();
        CustomUserDetails userDetails = new CustomUserDetails(usuario);

        String newAccessToken = jwtService.generateAccessToken(userDetails);
        storedToken.setRevocado(true);
        refreshTokenRepository.save(storedToken);

        String newRefreshTokenValue = jwtService.generateRefreshTokenValue(userDetails);
        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(newRefreshTokenValue)
                .usuario(usuario)
                .fechaExpiracion(LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpirationMs() / 1000))
                .revocado(false)
                .build();
        refreshTokenRepository.save(newRefreshToken);

        return LoginResponse.of(newAccessToken, newRefreshTokenValue, jwtService.getAccessTokenExpirationMs());
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(token -> {
                    token.setRevocado(true);
                    refreshTokenRepository.save(token);
                });
    }
}