package org.entur.auth.junit.tenant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to configure and generate a JSON Web Token (JWT) for a tenant.
 *
 * <p>This annotation can be applied to method parameters in JUnit tests to specify the token
 * properties for a tenant.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface TenantToken {

    /** The default tenant for the token. */
    String DEFAULT_TENANT = "myTenant";

    /** The default subject for the token. */
    String DEFAULT_SUBJECT = "mySubject";

    /** The default audience for the token. */
    String DEFAULT_AUDIENCE = "myAudience";

    /** The default expires in for the token. */
    long DEFAULT_EXPIRES_IN = 60000;

    /**
     * The tenant for the token.
     *
     * <p>Defaults to {@link #DEFAULT_TENANT}.
     *
     * @return the tenant
     */
    String tenant() default DEFAULT_TENANT;

    /**
     * The subject for the token.
     *
     * <p>Defaults to {@link #DEFAULT_SUBJECT}.
     *
     * @return the subject
     */
    String subject() default DEFAULT_SUBJECT;

    /**
     * The audience for the token.
     *
     * <p>Defaults to {@link #DEFAULT_AUDIENCE}.
     *
     * @return the audience
     */
    String[] audience() default DEFAULT_AUDIENCE;

    /**
     * The expiresIn for the token.
     *
     * <p>Defaults to {@link #DEFAULT_EXPIRES_IN}.
     *
     * @return the expiresIn
     */
    long expiresIn() default DEFAULT_EXPIRES_IN;

    /**
     * The string claims for the token.
     *
     * @return the claims of type string
     */
    StringClaim[] stringClaims() default {};

    /**
     * The array of string claims for the token.
     *
     * @return the array claims of type string
     */
    StringArrayClaim[] stringArrayClaims() default {};

    /**
     * The long claims for the token.
     *
     * @return the claims of type long
     */
    LongClaim[] longClaims() default {};

    /**
     * The array of long claims for the token.
     *
     * @return the array claims of type string
     */
    LongArrayClaim[] longArrayClaims() default {};

    /**
     * The array of long claims for the token.
     *
     * @return the array claims of type boolean
     */
    BooleanClaim[] booleanClaims() default {};
}
