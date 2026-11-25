
package ma.uae.faculte.auth.dto;

public record TokenResponse(
        String tokenType,
        String accessToken,
        String refreshToken,
        long expiresIn
) { }
