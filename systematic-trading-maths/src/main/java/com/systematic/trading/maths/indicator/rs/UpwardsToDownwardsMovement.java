package com.systematic.trading.maths.indicator.rs;

import java.math.BigDecimal;
import java.math.MathContext;

public class UpwardsToDownwardsMovement {

	/** Scale, precision and rounding to apply to mathematical operations. */
	private final MathContext mathContext;

	private BigDecimal upward;
	private BigDecimal downward;

	public UpwardsToDownwardsMovement( final MathContext mathContext ) {
		this.mathContext = mathContext;
		this.upward = BigDecimal.ZERO;
		this.downward = BigDecimal.ZERO;
	}

	public BigDecimal getUpward() {
		return upward;
	}

	public BigDecimal getDownward() {
		return downward;
	}

	public void addUpwards( final BigDecimal add ) {
		this.upward = upward.add(add, mathContext);
	}

	public void addDownwards( final BigDecimal add ) {
		this.downward = downward.add(add, mathContext);
	}

	public void divideUpwards( final BigDecimal division ) {
		this.upward = upward.divide(division, mathContext);
	}

	public void divideDownwards( final BigDecimal division ) {
		this.downward = downward.divide(division, mathContext);
	}
}
