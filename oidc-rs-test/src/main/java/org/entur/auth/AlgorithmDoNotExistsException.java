package org.entur.auth;

/**
 * Exception thrown when a requested cryptographic algorithm does not exist.
 */
public class AlgorithmDoNotExistsException extends RuntimeException {

	/**
	 * Constructs a new {@code AlgorithmDoNotExistsException} with the specified cause.
	 *
	 * @param e the cause of this exception
	 */
	public AlgorithmDoNotExistsException(Exception e) {
		super(e);
	}
}