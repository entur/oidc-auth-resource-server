package org.entur.auth.junit.tenant;

/**
 * Exception thrown when a parameter cannot be resolved by the TenantJsonWebToken extension.
 * <p>
 * This runtime exception is used to indicate that a parameter injection is not supported
 * or that the resolution of the parameter failed during the execution of tests.
 */
public class CanNotResolveParameterException extends RuntimeException {
}