
package ma.uae.faculte.auth.service;

import ma.uae.faculte.auth.config.JwtKeyConfig;
import ma.uae.faculte.auth.dto.TokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final UserDetailsService users;

    public TokenService(
            JwtEncoder encoder,
            JwtDecoder decoder,
            UserDetailsService users) {

        this.encoder = encoder;
        this.decoder = decoder;
        this.users = users;
    }

    public TokenResponse generate(UserDetails user) {
        String access = sign(user, "access", Duration.ofMinutes(15));
        String refresh = sign(user, "refresh", Duration.ofDays(7));

        return new TokenResponse("Bearer", access, refresh, 900);
    }

    public TokenResponse refresh(String refreshToken) {
        try {
            Jwt jwt = decoder.decode(refreshToken);

            if (!"refresh".equals(jwt.getClaimAsString("type"))) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Un refresh token est requis");
            }

            return generate(users.loadUserByUsername(jwt.getSubject()));

        } catch (JwtException | UsernameNotFoundException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Refresh token invalide ou expire",
                    ex);
        }
    }

    private String sign(
            UserDetails user,
            String type,
            Duration validity) {

        Instant now = Instant.now();

        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replaceFirst("^ROLE_", ""))
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(JwtKeyConfig.ISSUER)
                .subject(user.getUsername())
                .audience(List.of("faculte-api"))
                .issuedAt(now)
                .expiresAt(now.plus(validity))
                .id(UUID.randomUUID().toString())
                .claim("roles", roles)
                .claim("type", type)
                .build();

        JwsHeader header = JwsHeader
                .with(SignatureAlgorithm.RS256)
                .keyId("faculte-key-1")
                .build();

        return encoder.encode(
                        JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }
}
