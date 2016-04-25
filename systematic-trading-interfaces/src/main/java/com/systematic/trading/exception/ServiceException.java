package com.systematic.trading.exception;

/**
 * Base application level exception in the Systematic Trading application.
 * 
 * @author CJ Hare
 */
public abstract class ServiceException extends Exception {

	/** Serialization identity. */
	private static final long serialVersionUID = 1L;

	public ServiceException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ServiceException(final String message) {
		super(message);
	}
}