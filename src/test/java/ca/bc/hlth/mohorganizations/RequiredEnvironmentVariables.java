package ca.bc.hlth.mohorganizations;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstancePreConstructCallback;

import java.util.List;

/**
 * Fails the test class with a clear message when environment variables the tests need are missing.
 * <p>
 * Without this, a missing variable shows up as a long Spring context failure ("Could not resolve placeholder ...")
 * repeated for every test. This runs before the test instance is created, so before Spring starts.
 * <p>
 * A Java system property with the same name (-DNAME=value) also counts, because Spring accepts either.
 */
class RequiredEnvironmentVariables implements TestInstancePreConstructCallback {

    private static final List<String> REQUIRED = List.of(
            "ORGANIZATIONS_API_TOKEN_CREDENTIALS" // credentials for getting a token from DEV Keycloak
    );

    @Override
    public void preConstructTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext context) {
        List<String> missing = REQUIRED.stream()
                .filter(name -> isBlank(System.getenv(name)) && isBlank(System.getProperty(name)))
                .toList();
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Missing required environment variable(s): " + String.join(", ", missing)
                    + ". Set them in your shell or IntelliJ run configuration. See \"Running Tests\" in README.md.");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
