package com.systematic.trading.simulation.logic.trade;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Configuration for trade value that uses a relative value, percentage of the funds.
 * 
 * @author CJ Hare
 */
public class RelativeTradeValueConfiguration implements TradeValueConfiguration {

	/** Scale and precision to apply to mathematical operations. */
	private final MathContext mathContext;

	/** Value between zero and one, the percentage of the funds to use in a trade. */
	private final BigDecimal percentage;

	public RelativeTradeValueConfiguration(final BigDecimal percentage, final MathContext mathContext) {
		this.percentage = percentage;
		this.mathContext = mathContext;
	}

	@Override
	public BigDecimal getValue() {
		return percentage;
	}

	@Override
	public Type getType() {
		return Type.RELATIVE;
	}

	@Override
	public BigDecimal getTradeValue( final BigDecimal funds ) {
		return funds.multiply(percentage, mathContext);
	}
}