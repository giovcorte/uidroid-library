package com.uidroid.uidroid;

/**
 * Class representing an exception occurred at runtime while binding a view. Can be throw when a
 * view, configuration, composite or binder cannot be generated for a class type, or when an
 * inconsistency is detected in the current context.
 */
public class DatabindingException extends RuntimeException {

    public DatabindingException(String message) {
        super(message);
    }

}
