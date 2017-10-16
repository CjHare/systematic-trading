package com.systematic.trading.backtest.configuration.equity;

import java.math.BigDecimal;

public enum EquityManagementFeeConfiguration {
	NONE(),
	VGS(new BigDecimal[] { BigDecimal.valueOf(0) }, new BigDecimal[] { BigDecimal.valueOf(0.0018) });

	private final BigDecimal[] feeRange;
	private final BigDecimal[] percentageFee;

	EquityManagementFeeConfiguration() {
		this.feeRange = new BigDecimal[0];
		this.percentageFee = new BigDecimal[0];
	}

	EquityManagementFeeConfiguration( final BigDecimal[] feeRange, final BigDecimal[] percentageFee ) {
		this.feeRange = feeRange;
		this.percentageFee = percentageFee;
	}

	public BigDecimal[] getFeeRange() {
		return feeRange;
	}

	public BigDecimal[] getPercentageFee() {
		return percentageFee;
	}
}
