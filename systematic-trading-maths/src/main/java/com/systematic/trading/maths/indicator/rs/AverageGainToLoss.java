package com.systematic.trading.maths.indicator.rs;

import java.math.BigDecimal;
import java.math.MathContext;

public class AverageGainToLoss {

	/** Scale, precision and rounding to apply to mathematical operations. */
	private final MathContext mathContext;

	/** The divisor for the sum, used to gain the average. */
	private final BigDecimal division;

	private BigDecimal upward;
	private BigDecimal downward;

	public AverageGainToLoss( final BigDecimal division, final MathContext mathContext ) {
		this.mathContext = mathContext;
		this.division = division;
		this.upward = BigDecimal.ZERO;
		this.downward = BigDecimal.ZERO;
	}

	public BigDecimal getAverageGain() {
		return upward.divide(division, mathContext);
	}

	public BigDecimal getAverageLoss() {
		return downward.divide(division, mathContext);
	}

	/**
	 * Adding a gain, with an inferred loss of zero.
	 */
	public void addGain( final BigDecimal gain ) {
		this.upward = upward.add(gain, mathContext);
	}

	/**
	 * Adding a loss, with an inferred gain of zero.
	 */
	public void addLoss( final BigDecimal loss ) {
		this.downward = downward.add(loss, mathContext);
	}
}
