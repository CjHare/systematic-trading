package com.systematic.trading.backtest.exception;

/**
 * Problem occurred during the initialization phase of back test.
 * 
 * @author CJ Hare
 */
public class BacktestInitialisationException extends Exception {

	/** Default serial ID. */
	private static final long serialVersionUID = 1L;

	public BacktestInitialisationException(final Exception cause) {
		super(cause);
	}
}
