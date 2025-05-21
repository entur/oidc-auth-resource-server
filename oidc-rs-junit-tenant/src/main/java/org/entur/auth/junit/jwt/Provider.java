package org.entur.auth.junit.jwt;

/**
 * Represents an authentication provider that supports multiple tenants and provides utility methods
 * for handling authentication-related information.
 */
public interface Provider {
    /** Default key identifier. */
    String KEY_ID_DEFAULT = "12345678";

    /** Claim name for authorized party. */
    String CLAIM_AZP = "azp";

    /** Claim name for subject. */
    String CLAIM_SUB = "sub";

    /** Claim name for preferred username. */
    String CLAIM_PREFERRED_USERNAME = "preferred_username";

    /** Claim name for email. See RFC 5322. */
    String CLAIM_EMAIL = "email";

    /** Claim name for email verification. */
    String CLAIM_EMAIL_VERIFIED = "email_verified";

    /**
     * Returns the name of the authentication provider.
     *
     * @return the name of the provider, defaulting to "auth0".
     */
    default String getName() {
        return "provider";
    }

    /**
     * Returns the issuer URL for a given tenant.
     *
     * @param tenant the tenant name
     * @return the issuer URL for the specified tenant
     * @throws IllegalArgumentException if the tenant is not recognized
     */
    default String getIssuerUrl(String tenant) {
        return String.format("https://mock/auth/realms/%s", tenant);
    }

    /**
     * Returns the certificate path for a given tenant.
     *
     * @param tenant the tenant name
     * @return the certificate path for the specified tenant
     * @throws IllegalArgumentException if the tenant is not recognized
     */
    default String getCertPath(String tenant) {
        return String.format("/%s/.well-known/jwks.json", tenant);
    }
}
