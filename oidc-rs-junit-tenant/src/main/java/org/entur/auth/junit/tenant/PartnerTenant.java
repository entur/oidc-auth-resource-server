package org.entur.auth.junit.tenant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to configure and generate a JSON Web Token (JWT) for a partner tenant.
 * <p>
 * This annotation can be applied to method parameters in JUnit tests to specify the token properties
 * for a partner tenant. It defines default values for client identifier, organisation identifier,
 * username, audience, email, subject, email verification status, permissions, and token validity duration.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface PartnerTenant {

    /**
     * The default client identifier for a partner tenant.
     */
    String DEFAULT_CLIENT_ID = "myPartnerClientId";

    /**
     * The default organisation identifier for a partner tenant.
     */
   long DEFAULT_ORGANISATION_ID = 123L;

    /**
     * The default username for a partner tenant.
     */
    String DEFAULT_USERNAME = "myPartnerUsername";

    /**
     * The default token validity duration in minutes.
     */
    int DEFAULT_TOKEN_VALID_IN_MINUTES = 5;

    /**
     * The default audience for the token.
     */
    String DEFAULT_AUDIENCE = "https://api.dev.entur.io";

    /**
     * The default email address for a partner tenant.
     */
    String DEFAULT_EMAIL = "myPartner@email.com";

    /**
     * The default email verification status for a partner tenant.
     */
    boolean DEFAULT_EMAIL_VERIFIED = true;

    /**
     * The default subject for the token.
     */
    String DEFAULT_SUBJECT = "myPartnerSubject";

    /**
     * The organisation identifier for the partner tenant.
     * <p>
     * Defaults to {@link #DEFAULT_ORGANISATION_ID}.
     * </p>
     *
     * @return the organisation id
     */
    long organisationId() default DEFAULT_ORGANISATION_ID;

    /**
     * The client identifier for the partner tenant.
     * <p>
     * Defaults to {@link #DEFAULT_CLIENT_ID}.
     * </p>
     *
     * @return the client id
     */
    String clientId() default DEFAULT_CLIENT_ID;

    /**
     * The username for the partner tenant.
     * <p>
     * Defaults to {@link #DEFAULT_USERNAME}.
     * </p>
     *
     * @return the username
     */
    String username() default DEFAULT_USERNAME;

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
     * The email address for the partner tenant.
     * <p>
     * Defaults to {@link #DEFAULT_EMAIL}.
     * </p>
     *
     * @return the email address
     */
    String email() default DEFAULT_EMAIL;

    /**
     * The subject for the token.
     * <p>
     * Defaults to {@link #DEFAULT_SUBJECT}.
     * </p>
     *
     * @return the subject
     */
    String subject() default DEFAULT_SUBJECT;

    /**
     * Indicates whether the email is verified.
     * <p>
     * Defaults to {@link #DEFAULT_EMAIL_VERIFIED}.
     * </p>
     *
     * @return {@code true} if the email is verified; {@code false} otherwise
     */
    boolean emailVerified() default DEFAULT_EMAIL_VERIFIED;

    /**
     * The permissions associated with the partner tenant.
     * <p>
     * Defaults to an empty array.
     * </p>
     *
     * @return an array of permission strings
     */
    String[] permissions() default {};

    /**
     * The token validity duration in minutes.
     * <p>
     * Defaults to {@link #DEFAULT_TOKEN_VALID_IN_MINUTES}.
     * </p>
     *
     * @return the token expiration time in minutes
     */
    int expiresInMinutes() default DEFAULT_TOKEN_VALID_IN_MINUTES;

}
