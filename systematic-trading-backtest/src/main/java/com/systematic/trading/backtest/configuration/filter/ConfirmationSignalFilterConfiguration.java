package com.systematic.trading.backtest.configuration.filter;

import com.systematic.trading.backtest.configuration.signals.SignalConfiguration;

public class ConfirmationSignalFilterConfiguration {

	public enum Type {
		DELAY_ONE_DAY_RANGE_THREE_DAYS(1, 3),
		DELAY_ONE_DAY_RANGE_FOUR_DAYS(1, 4);

		private final int confirmationDayRange;
		private final int delayUntilConfirmationRange;

		Type(final int delayUntilConfirmationRange, final int confirmationDayRange) {
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

	private final SignalConfiguration anchor;
	private final SignalConfiguration confirmation;
	private final Type type;

	public ConfirmationSignalFilterConfiguration(final Type type, final SignalConfiguration anchor,
	        SignalConfiguration confirmation) {
		this.type = type;
		this.anchor = anchor;
		this.confirmation = confirmation;
	}

	public SignalConfiguration getAnchor() {
		return anchor;
	}

	public SignalConfiguration getConfirmation() {
		return confirmation;
	}

	public Type getType() {
		return type;
	}
}