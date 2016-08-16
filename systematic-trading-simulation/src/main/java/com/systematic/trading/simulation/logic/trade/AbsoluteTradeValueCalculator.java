package com.systematic.trading.simulation.logic.trade;

import java.math.BigDecimal;

/**
 * Configuration for trade value that uses an absolute value.
 * 
 * @author CJ Hare
 */
public class AbsoluteTradeValueCalculator implements TradeValueCalculator {

	/** The only value to ever use. */
	private final BigDecimal value;

	public AbsoluteTradeValueCalculator(final BigDecimal value) {
		this.value = value;
	}

	@Override
	public BigDecimal getValue() {
		return value;
	}

	@Override
	public Type getType() {
		return Type.ABSOLUTE;
	}

	@Override
	public BigDecimal getTradeValue( final BigDecimal funds ) {
		return value;
	}
}