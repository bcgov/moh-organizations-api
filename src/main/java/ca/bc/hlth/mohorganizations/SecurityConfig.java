package ca.bc.hlth.mohorganizations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${organization-api-client-id}")
    private String apiClientName;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakClientRoleConverter(apiClientName));

        http.authorizeRequests()
                .mvcMatchers(HttpMethod.GET, "/organizations").hasRole("get-org")
                .mvcMatchers(HttpMethod.GET, "/organizations/{resourceId}").hasRole("get-org")
                .mvcMatchers(HttpMethod.POST, "/organizations").hasRole("add-org")
                .mvcMatchers(HttpMethod.PUT, "/organizations/{resourceId}").hasRole("add-org")
                .anyRequest().denyAll()
                .and().cors()
                .and().oauth2ResourceServer().jwt()
                .jwtAuthenticationConverter(jwtAuthenticationConverter);
    }

    @Bean
    public JwtDecoder customDecoder(OAuth2ResourceServerProperties properties) {
        return NimbusJwtDecoder
                .withJwkSetUri(properties.getJwt().getJwkSetUri())
                .build();
    }
}