
package ma.uae.faculte.auth.controller;

import jakarta.validation.Valid;
import ma.uae.faculte.auth.dto.*;
import ma.uae.faculte.auth.service.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class OAuth2Controller {

    private final AuthenticationManager authManager;
    private final TokenService tokens;

    public OAuth2Controller(
            AuthenticationManager authManager,
            TokenService tokens) {

        this.authManager = authManager;
        this.tokens = tokens;
    }

    @PostMapping("/login")
    public TokenResponse login(
            @Valid @RequestBody LoginRequest request) {

        try {
            var authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()));

            return tokens.generate(
                    (UserDetails) authentication.getPrincipal());

        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Identifiants invalides");
        }
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(
            @Valid @RequestBody RefreshRequest request) {

        return tokens.refresh(request.refreshToken());
    }
}
