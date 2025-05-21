package org.entur.auth.junit.jwt;

/**
 * Implementation of the {@link Provider} interface for Entur authentication.
 *
 * <p>This provider defines tenant-specific constants and methods for generating issuer URLs and
 * mapping claim names for authentication purposes.
 *
 * <p>Tenant identifiers:
 *
 * <ul>
 *   <li>{@link #TENANT_INTERNAL} - Represents an internal tenant.
 *   <li>{@link #TENANT_PARTNER} - Represents a partner tenant.
 *   <li>{@link #TENANT_TRAVELLER} - Represents a traveller tenant.
 *   <li>{@link #TENANT_PERSON} - Represents a person tenant.
 * </ul>
 *
 * <p>Claim constants:
 *
 * <ul>
 *   <li>{@link #CLAIM_ORGANISATION_ID}
 *   <li>{@link #CLAIM_CUSTOMER_NUMBER}
 *   <li>{@link #CLAIM_SOCIAL_SECURITY_NUMBER}
 *   <li>{@link #CLAIM_PERMISSIONS} - Claim name for user permissions.
 * </ul>
 */
public class EnturProvider implements Provider {
    /** Tenant identifier for internal users. */
    public static final String TENANT_INTERNAL = "internal";

    /** Tenant identifier for partner users. */
    public static final String TENANT_PARTNER = "partner";

    /** Tenant identifier for traveller users. */
    public static final String TENANT_TRAVELLER = "traveller";

    /** Tenant identifier for person users. */
    public static final String TENANT_PERSON = "person";

    /** Claim key for organisation ID. */
    public static final String CLAIM_ORGANISATION_ID = "https://entur.io/organisationID";

    /** Claim key for customer number. */
    public static final String CLAIM_CUSTOMER_NUMBER = "https://entur.io/customerNumber";

    /** Claim key for social security number. */
    public static final String CLAIM_SOCIAL_SECURITY_NUMBER = "https://entur.io/ssn";

    /** Claim key for user permissions. */
    public static final String CLAIM_PERMISSIONS = "permissions";

    /**
     * Returns the name of the authentication provider.
     *
     * @return the provider name as a {@link String}
     */
    public String getName() {
        return "auth0";
    }

    /**
     * Generates and returns the issuer URL for the specified tenant.
     *
     * @param tenant the tenant identifier
     * @return the issuer URL formatted as "https://{tenant}.mock.entur.io"
     */
    public String getIssuerUrl(String tenant) {
        return String.format("https://%s.mock.entur.io", tenant);
    }
}
