
package ma.uae.faculte.auth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtKeyConfig {

    public static final String ISSUER = "http://localhost:9100";

    @Bean
    RSAKey rsaKey() throws Exception {
        KeyFactory factory = KeyFactory.getInstance("RSA");

        byte[] privateBytes = readPem("keys/private.pem");
        byte[] publicBytes = readPem("keys/public.pem");

        RSAPrivateKey privateKey = (RSAPrivateKey) factory.generatePrivate(
                new PKCS8EncodedKeySpec(privateBytes));

        RSAPublicKey publicKey = (RSAPublicKey) factory.generatePublic(
                new X509EncodedKeySpec(publicBytes));

        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("faculte-key-1")
                .build();
    }

    private static byte[] readPem(String file) throws Exception {
        String base64 = Files.readString(Path.of(file))
                .replaceAll("-----[^-]+-----", "")
                .replaceAll("\\s", "");

        return Base64.getDecoder().decode(base64);
    }

    @Bean
    JwtEncoder jwtEncoder(RSAKey rsaKey) {
        return new NimbusJwtEncoder(
                new ImmutableJWKSet<>(new JWKSet(rsaKey)));
    }

    @Bean
    JwtDecoder jwtDecoder(RSAKey rsaKey) throws Exception {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withPublicKey(rsaKey.toRSAPublicKey())
                .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(ISSUER));

        return decoder;
    }
}
