package com.neobankx.auth.api;

import com.neobankx.auth.application.AuthService;
import com.neobankx.auth.domain.AuthTokenPair;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    AuthTokenResponse register(@Valid @RequestBody RegisterRequest request) {
        return AuthTokenResponse.from(authService.register(request.email(), request.fullName(), request.password()));
    }

    @PostMapping("/login")
    AuthTokenResponse login(@Valid @RequestBody LoginRequest request) {
        return AuthTokenResponse.from(authService.login(request.email(), request.password()));
    }

    @PostMapping("/refresh")
    AuthTokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return AuthTokenResponse.from(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
    }

    @GetMapping("/me")
    AuthenticatedUserResponse me(@AuthenticationPrincipal Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return new AuthenticatedUserResponse(
                jwt.getSubject(),
                jwt.getClaimAsString("email"),
                roles == null ? List.of() : roles
        );
    }

    record AuthTokenResponse(
            String accessToken,
            Instant accessTokenExpiresAt,
            String refreshToken,
            Instant refreshTokenExpiresAt,
            String tokenType
    ) {
        static AuthTokenResponse from(AuthTokenPair pair) {
            return new AuthTokenResponse(
                    pair.accessToken(),
                    pair.accessTokenExpiresAt(),
                    pair.refreshToken(),
                    pair.refreshTokenExpiresAt(),
                    pair.tokenType()
            );
        }
    }
}

