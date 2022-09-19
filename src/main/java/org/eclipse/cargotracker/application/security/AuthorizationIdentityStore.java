package org.eclipse.cargotracker.application.security;

import javax.enterprise.context.ApplicationScoped;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.security.enterprise.identitystore.IdentityStore.ValidationType.PROVIDE_GROUPS;

@ApplicationScoped
public class AuthorizationIdentityStore implements IdentityStore {
    private static final Logger LOGGER = Logger.getLogger(AuthorizationIdentityStore.class.getName());

    private final Map<String, Set<String>> authorization = Map.of(
            "user", Set.of("ADMIN", "USER"),
            "auth0|63249de53e292439f31c6be6", Set.of("ADMIN", "USER"),
            "user@example.com",  Set.of("ADMIN", "USER"));// user in okta.

    @Override
    public Set<ValidationType> validationTypes() {
        return EnumSet.of(PROVIDE_GROUPS);
    }

    @Override
    public Set<String> getCallerGroups(CredentialValidationResult validationResult) {
        var principal = validationResult.getCallerPrincipal().getName();
        LOGGER.log(Level.INFO, "Get principal name in validation result: {0}", principal);
        return authorization.get(principal) == null ? Collections.<String>emptySet() : authorization.get(principal);
    }

}