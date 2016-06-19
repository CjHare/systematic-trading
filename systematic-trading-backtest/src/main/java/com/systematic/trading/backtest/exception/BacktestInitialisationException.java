package com.systematic.trading.backtest.exception;

import com.systematic.trading.exception.ServiceException;

/**
 * Problem occurred during the initialization phase of back test.
 * 
 * @author CJ Hare
 */
public class BacktestInitialisationException extends ServiceException {

	/** Default serial ID. */
	private static final long serialVersionUID = 1L;

	public BacktestInitialisationException(final Exception cause) {
		super(cause);
	}
}
