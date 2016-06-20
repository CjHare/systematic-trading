package com.systematic.trading.maths.formula.rs;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Single data point in a series of relative strength values.
 * 
 * @author CJ Hare
 */
public class RelativeStrengthDataPoint {

	private final BigDecimal value;
	private final LocalDate date;

	public RelativeStrengthDataPoint(final LocalDate date, final BigDecimal value) {
		this.date = date;
		this.value = value;
	}

	/**
	 * RS value.
	 * 
	 * @return RS value on the given data point date.
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
