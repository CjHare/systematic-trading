package com.systematic.trading.simulation.logic.trade;

import java.math.BigDecimal;

/**
 * Details of a specific trading behaviour and how it's calculated.
 * 
 * @author CJ Hare
 */
public interface TradeValueCalculator {

	/**
	 * ABSOLUTE is taken as the actual spend in currency.
	 * RELATIVE is to the total of funds available.
	 */
	enum Type {
		ABSOLUTE,
		RELATIVE;
	}

	/**
	 * Configuration value used in calculating a trade amount.
	 * 
	 * @return value whose application is defined by the type.
	 */
	BigDecimal getValue();

	/**
	 * Defines the behaviour applied with the value to calculate the trade amount.
	 * 
	 * @return how the value is used in the calculating the amount of equities to trade.
	 */
	Type getType();

	/**
	 * Calculates the appropriate trade value for the given funds.
	 * 
	 * @param funds total trading funds.
	 * @return how much to spend on this trade.
	 */
	BigDecimal getTradeValue( BigDecimal funds );
}
