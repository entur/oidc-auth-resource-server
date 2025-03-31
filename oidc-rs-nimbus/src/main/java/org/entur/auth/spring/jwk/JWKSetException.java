package org.entur.auth.spring.jwk;

/**
 * JWKSetException is a custom RuntimeException used to represent exceptions related to JWK (JSON Web Key) sets.
 */
public class JWKSetException extends RuntimeException {
    /**
     * Constructs a new JWKSetException with the specified error message.
     *
     * @param message The error message describing the cause of the exception.
     */
    public JWKSetException(String message) {
        super(message);
    }

    /**
     * Constructs a new JWKSetException with the specified error message and the cause of the exception.
     *
     * @param message The error message describing the cause of the exception.
     * @param cause   The cause of the exception (can be another throwable that caused this exception).
     */
    public JWKSetException(String message, Throwable cause) {
        super(message, cause);
    }
}
