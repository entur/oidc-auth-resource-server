package org.entur.auth.junit.tenant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to configure and generate a JSON Web Token (JWT) for a traveller tenant.
 * <p>
 * This annotation can be applied to method parameters in JUnit tests to specify the token properties
 * for a traveller tenant. It provides default values for the client identifier, organisation identifier,
 * customer number, audience, and the token validity duration.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface TravellerTenant {

    /**
     * The default client identifier for a traveller tenant.
     */
    String DEFAULT_CLIENT_ID = "myPartnerClientId";

    /**
     * The default organisation identifier for a traveller tenant.
     */
    long DEFAULT_ORGANISATION_ID = 123L;

    /**
     * The default customer number for a traveller tenant.
     */
    String DEFAULT_CUSTOMER_NUMBER = "myCustomerNumber";

    /**
     * The default token validity duration in minutes.
     */
    int DEFAULT_TOKEN_VALID_IN_MINUTES = 5;

    /**
     * The default audience for the token.
     */
    String DEFAULT_AUDIENCE = "https://api.dev.entur.io";

    /**
     * Returns the organisation identifier for the traveller tenant.
     * <p>
     * Defaults to {@link #DEFAULT_ORGANISATION_ID}.
     * </p>
     *
     * @return the organisation id
     */
    long organisationId() default DEFAULT_ORGANISATION_ID;

    /**
     * Returns the client identifier for the traveller tenant.
     * <p>
     * Defaults to {@link #DEFAULT_CLIENT_ID}.
     * </p>
     *
     * @return the client id
     */
    String clientId() default DEFAULT_CLIENT_ID;

    /**
     * Returns the customer number for the traveller tenant.
     * <p>
     * Defaults to {@link #DEFAULT_CUSTOMER_NUMBER}.
     * </p>
     *
     * @return the customer number
     */
    String customerNumber() default DEFAULT_CUSTOMER_NUMBER;

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
