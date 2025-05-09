package org.entur.auth.spring.web;

/** Custom exception class for handling configuration issues. */
public class ResourceServerConfigurationException extends Exception {

    /**
     * Constructs a new ResourceServerConfigurationException with the specified cause.
     *
     * @param ex The exception that caused this configuration issue.
     */
    public ResourceServerConfigurationException(Exception ex) {
        super(ex);
    }
}
