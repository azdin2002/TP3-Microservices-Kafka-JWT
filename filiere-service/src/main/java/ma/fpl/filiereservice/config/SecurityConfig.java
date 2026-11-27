
package ma.fpl.filiereservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.*;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    JwtDecoder jwtDecoder(
            @Value("${security.jwks-uri}") String jwks,
            @Value("${security.issuer}") String issuer) {

        NimbusJwtDecoder decoder =
                NimbusJwtDecoder.withJwkSetUri(jwks).build();

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        JwtValidators.createDefaultWithIssuer(issuer),

                        new JwtClaimValidator<>(
                                "type",
                                "access"::equals
                        ),

                        new JwtClaimValidator<>(
                                "aud",
                                aud -> aud instanceof java.util.List<?> list
                                        && list.contains("faculte-api")
                        )
                )
        );

        return decoder;
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter roles =
                new JwtGrantedAuthoritiesConverter();

        roles.setAuthoritiesClaimName("roles");
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(roles);

        return converter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter converter) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/error"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/filieres").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/filieres/**").authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/filieres"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                "/api/filieres/**"
                        ).hasRole("ADMIN")

                        .anyRequest().denyAll()
                )

                .oauth2ResourceServer(oauth ->
                        oauth.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(converter)
                        )
                )

                .build();
    }
}
