package com.systematic.trading.backtest.configuration.filter;

public enum ConfirmationSignalFilterConfiguration {
	DELAY_ONE_DAY_RANGE_THREE_DAYS(1, 3),
	DELAY_ONE_DAY_RANGE_FOUR_DAYS(1, 4);

	private final int confirmationDayRange;
	private final int delayUntilConfirmationRange;

	ConfirmationSignalFilterConfiguration(final int confirmationDayRange, final int delayUntilConfirmationRange) {
		this.confirmationDayRange = confirmationDayRange;
		this.delayUntilConfirmationRange = delayUntilConfirmationRange;
	}

	public int getConfirmationDayRange() {
		return confirmationDayRange;
	}

	public int getDelayUntilConfirmationRange() {
		return delayUntilConfirmationRange;
	}
}