package org.entur.auth.spring.webflux;

/** Custom exception class for handling configuration issues. */
public class ReactiveResourceServerConfigurationException extends Exception {

    /**
     * Constructs a new ResourceServerConfigurationException with the specified cause.
     *
     * @param ex The exception that caused this configuration issue.
     */
    public ReactiveResourceServerConfigurationException(Exception ex) {
        super(ex);
    }
}
