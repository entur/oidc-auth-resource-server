package org.entur.auth.junit.tenant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to configure and generate a JSON Web Token (JWT) for a person tenant.
 * <p>
 * This annotation can be applied to method parameters in JUnit tests to specify the token properties
 * for a person tenant. Default values are provided for client identifier, organisation identifier,
 * social security number, audience, and token validity duration.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface PersonTenant {

    /**
     * The default client identifier for a person tenant.
     */
    String DEFAULT_CLIENT_ID = "myPersonClientId";

    /**
     * The default organisation identifier for a person tenant.
     */
    long DEFAULT_ORGANISATION_ID = 123L;

    /**
     * The default social security number for a person tenant.
     */
    String DEFAULT_SOCIAL_SECURITY_NUMBER = "11223355555";

    /**
     * The default token validity duration in minutes.
     */
    int DEFAULT_TOKEN_VALID_IN_MINUTES = 5;

    /**
     * The default audience for the token.
     */
    String DEFAULT_AUDIENCE = "https://api.dev.entur.io";

    /**
     * Returns the organisation identifier for the person tenant.
     * <p>
     * Defaults to {@link #DEFAULT_ORGANISATION_ID}.
     * </p>
     *
     * @return the organisation id
     */
    long organisationId() default DEFAULT_ORGANISATION_ID;

    /**
     * Returns the client identifier for the person tenant.
     * <p>
     * Defaults to {@link #DEFAULT_CLIENT_ID}.
     * </p>
     *
     * @return the client id
     */
    String clientId() default DEFAULT_CLIENT_ID;

    /**
     * Returns the social security number for the person tenant.
     * <p>
     * Defaults to {@link #DEFAULT_SOCIAL_SECURITY_NUMBER}.
     * </p>
     *
     * @return the social security number
     */
    String socialSecurityNumber() default DEFAULT_SOCIAL_SECURITY_NUMBER;

    /**
     * Returns the audience for the token.
     * <p>
     * Defaults to {@link #DEFAULT_AUDIENCE}.
     * </p>
     *
     * @return the audience
     */
    String audience() default DEFAULT_AUDIENCE;

    /**
     * Returns the token validity duration in minutes.
     * <p>
     * Defaults to {@link #DEFAULT_TOKEN_VALID_IN_MINUTES}.
     * </p>
     *
     * @return the token expiration time in minutes
     */
    int expiresInMinutes() default DEFAULT_TOKEN_VALID_IN_MINUTES;

}
