package com.systematic.trading.backtest.configuration.entry;

import com.systematic.trading.backtest.configuration.filter.ConfirmationSignalFilterConfiguration;
import com.systematic.trading.backtest.configuration.filter.PeriodicFilterConfiguration;
import com.systematic.trading.backtest.configuration.filter.SameDayFilterConfiguration;

public class EntryLogicConfiguration {

	public enum Type {
		PERIODIC,
		CONFIRMATION_SIGNAL,
		SAME_DAY_SIGNALS;
	}

	private final Type type;
	private final PeriodicFilterConfiguration periodic;
	private final ConfirmationSignalFilterConfiguration confirmationSignal;
	private final SameDayFilterConfiguration sameDaySignals;

	public EntryLogicConfiguration(final PeriodicFilterConfiguration periodic) {
		this.type = Type.PERIODIC;
		this.periodic = periodic;
		this.confirmationSignal = null;
		this.sameDaySignals = null;
	}

	public EntryLogicConfiguration(final ConfirmationSignalFilterConfiguration confirmationSignal) {
		this.type = Type.CONFIRMATION_SIGNAL;
		this.periodic = null;
		this.confirmationSignal = confirmationSignal;
		this.sameDaySignals = null;
	}

	public EntryLogicConfiguration(final SameDayFilterConfiguration sameDaySignals) {
		this.type = Type.SAME_DAY_SIGNALS;
		this.periodic = null;
		this.confirmationSignal = null;
		this.sameDaySignals = sameDaySignals;
	}

	public Type getType() {
		return type;
	}

	public PeriodicFilterConfiguration getPeriodic() {
		return periodic;
	}

	public ConfirmationSignalFilterConfiguration getConfirmationSignal() {
		return confirmationSignal;
	}

	public SameDayFilterConfiguration getSameDaySignals() {
		return sameDaySignals;
	}
}