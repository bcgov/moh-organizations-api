package ca.bc.hlth.mohorganizations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

// Credit: https://wstutorial.com/rest/spring-security-oauth2-keycloak-roles.html
@SuppressWarnings("unchecked")
public class KeycloakClientRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final String apiClientName;

    public KeycloakClientRoleConverter(String apiClientName) {
        this.apiClientName = Objects.requireNonNull(apiClientName);
    }

    public Collection<GrantedAuthority> convert(final Jwt jwt) {

        Collection<GrantedAuthority> authorities = null;

        /*
        The roles in the access token look like this:
        "resource_access": {
          "user-management-service": {
            "roles": [
               "view-groups"
            ]
          }
        }*/
        Map<String, Object> resourceAccesses = (Map<String, Object>) jwt.getClaims().get("resource_access");
        if (resourceAccesses != null) {
            Map<String, Object> resource = (Map<String, Object>) resourceAccesses.get(apiClientName);
            if (resource != null) {
                authorities = ((List<String>) resource.get("roles")).stream()
                        .map(roleName -> "ROLE_" + roleName) // prefix required to map to a Spring Security "role"
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            }
        }
        return authorities;
    }
}
