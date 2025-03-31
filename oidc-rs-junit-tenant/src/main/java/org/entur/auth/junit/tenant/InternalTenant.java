package org.entur.auth.junit.tenant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to configure and generate a JSON Web Token (JWT) for an internal tenant.
 * <p>
 * This annotation can be applied to method parameters in JUnit tests to specify the token properties
 * for an internal tenant. The provided values include the organisation ID, client ID, audience, and
 * the token's validity duration. Default values are defined for all properties.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface InternalTenant {

    /**
     * The default client identifier for an internal tenant.
     */
    String DEFAULT_CLIENT_ID = "myPartnerClientId";

    /**
     * The default organisation identifier for an internal tenant.
     */
    long DEFAULT_ORGANISATION_ID = 123L;

    /**
     * The default token validity duration in minutes.
     */
    int DEFAULT_TOKEN_VALID_IN_MINUTES = 5;

    /**
     * The default audience for the token.
     */
    String DEFAULT_AUDIENCE = "https://api.dev.entur.io";

    /**
     * The organisation identifier for the internal tenant.
     * <p>
     * Defaults to {@link #DEFAULT_ORGANISATION_ID}.
     * </p>
     *
     * @return the organisation id
     */
    long organisationId() default DEFAULT_ORGANISATION_ID;

    /**
     * The client identifier for the internal tenant.
     * <p>
     * Defaults to {@link #DEFAULT_CLIENT_ID}.
     * </p>
     *
     * @return the client id
     */
    String clientId() default DEFAULT_CLIENT_ID;

    /**
     * The audience for the token.
     * <p>
     * Defaults to {@link #DEFAULT_AUDIENCE}.
     * </p>
     *
     * @return the audience
     */
    String audience() default DEFAULT_AUDIENCE;

    /**
     * The token validity period in minutes.
     * <p>
     * Defaults to {@link #DEFAULT_TOKEN_VALID_IN_MINUTES}.
     * </p>
     *
     * @return the token expiration time in minutes
     */
    int expiresInMinutes() default DEFAULT_TOKEN_VALID_IN_MINUTES;
}
