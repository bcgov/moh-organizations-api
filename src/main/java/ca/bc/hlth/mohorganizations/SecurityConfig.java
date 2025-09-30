package ca.bc.hlth.mohorganizations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Value("${organization-api-client-id}")
    private String apiClientName;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
                new KeycloakClientRoleConverter(apiClientName));

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/organizations").hasRole("get-org")
                        .requestMatchers(HttpMethod.GET, "/organizations/{resourceId}").hasRole("get-org")
                        .requestMatchers(HttpMethod.POST, "/organizations").hasRole("add-org")
                        .requestMatchers(HttpMethod.PUT, "/organizations/{resourceId}").hasRole("add-org")
                        .requestMatchers(HttpMethod.DELETE, "/organizations/{resourceId}").hasRole("delete-org")
                        .requestMatchers(HttpMethod.GET, "/health").permitAll()
                        .anyRequest().denyAll()
                )
                .cors(cors -> {}) // keeps CORS enabled with defaults
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                );

        return http.build();
    }

    @Bean
    public JwtDecoder customDecoder(OAuth2ResourceServerProperties properties) {
        return NimbusJwtDecoder
                .withJwkSetUri(properties.getJwt().getJwkSetUri())
                .build();
    }
}
