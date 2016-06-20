package com.systematic.trading.maths.indicator.rsi;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Point for a RSI chart coordinate.
 * 
 * @author CJ Hare
 */
public class RelativeStrengthIndexDataPoint {

	private final BigDecimal value;
	private final LocalDate date;

	public RelativeStrengthIndexDataPoint(final LocalDate date, final BigDecimal value) {
		this.date = date;
		this.value = value;
	}

	/**
	 * RSI value.
	 * 
	 * @return RSI value on the given data point date.
	 */
	public BigDecimal getValue() {
		return value;
	}

	/**
	 * When the data point occurred.
	 * 
	 * @return date of the RSI value.
	 */
	public LocalDate getDate() {
		return date;
	}
}
