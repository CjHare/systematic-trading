package com.systematic.trading.backtest.configuration.filter;

import java.time.Period;

public enum PeriodicFilterConfiguration {
	WEEKLY(Period.ofWeeks(1)),
	MONTHLY(Period.ofMonths(1));

	private final Period frequency;

	PeriodicFilterConfiguration(final Period frequency) {
		this.frequency = frequency;
	}

	public Period getFrequency() {
		return frequency;
	}
}