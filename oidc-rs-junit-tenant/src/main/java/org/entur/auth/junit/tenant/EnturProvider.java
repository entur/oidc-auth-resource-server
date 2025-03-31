package org.entur.auth.junit.tenant;

import org.entur.auth.Provider;

/**
 * Implementation of the {@link Provider} interface for Entur authentication.
 * <p>
 * This provider defines tenant-specific constants and methods for generating issuer URLs
 * and mapping claim names for authentication purposes.
 * </p>
 * <p>
 * Tenant identifiers:
 * <ul>
 *   <li>{@link #TENANT_INTERNAL} - Represents an internal tenant.</li>
 *   <li>{@link #TENANT_PARTNER} - Represents a partner tenant.</li>
 *   <li>{@link #TENANT_TRAVELLER} - Represents a traveller tenant.</li>
 *   <li>{@link #TENANT_PERSON} - Represents a person tenant.</li>
 * </ul>
 * </p>
 * <p>
 * Claim constants:
 * <ul>
 *   <li>{@link #CLAIM_ORGANISATION_ID}</li>
 *   <li>{@link #CLAIM_CUSTOMER_NUMBER}</li>
 *   <li>{@link #CLAIM_SOCIAL_SECURITY_NUMBER}</li>
 *   <li>{@link #CLAIM_PERMISSIONS} - Claim name for user permissions.</li>
 * </ul>
 * </p>
 */
public class EnturProvider implements Provider {
	/**
	 * Tenant identifier for internal users.
	 */
	public static final String TENANT_INTERNAL = "internal";

	/**
	 * Tenant identifier for partner users.
	 */
	public static final String TENANT_PARTNER = "partner";

	/**
	 * Tenant identifier for traveller users.
	 */
	public static final String TENANT_TRAVELLER = "traveller";

	/**
	 * Tenant identifier for person users.
	 */
	public static final String TENANT_PERSON = "person";

	/**
	 * Claim key for organisation ID.
	 */
	public static final String CLAIM_ORGANISATION_ID = "organisationID";

	/**
	 * Claim key for customer number.
	 */
	public static final String CLAIM_CUSTOMER_NUMBER = "customerNumber";

	/**
	 * Claim key for social security number.
	 */
	public static final String CLAIM_SOCIAL_SECURITY_NUMBER = "ssn";

	/**
	 * Claim key for user permissions.
	 */
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

	/**
	 * Maps a given claim name to a standardized claim identifier.
	 * <p>
	 * If the claim name matches one of the predefined standard claim names (e.g.,
	 * {@code CLAIM_AZP}, {@code CLAIM_PREFERRED_USERNAME}, {@code CLAIM_EMAIL},
	 * or {@code CLAIM_EMAIL_VERIFIED}), the original name is returned.
	 * Otherwise, the claim name is mapped to a namespaced identifier using the format
	 * "https://entur.io/{name}".
	 * </p>
	 *
	 * @param name the original claim name
	 * @return the mapped claim name
	 */
	public String mapClaimName(String name) {
		if (CLAIM_AZP.equals(name)
				|| CLAIM_PREFERRED_USERNAME.equals(name)
				|| CLAIM_EMAIL.equals(name)
				|| CLAIM_EMAIL_VERIFIED.equals(name)
		) {
			return name;
		 } else {
			return String.format("https://entur.io/%s", name);
		}
	}
}
