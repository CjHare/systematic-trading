package com.systematic.trading.backtest.configuration.entry;

import com.systematic.trading.backtest.configuration.filter.ConfirmationSignalFilterConfiguration;
import com.systematic.trading.backtest.configuration.filter.PeriodicFilterConfiguration;
import com.systematic.trading.backtest.configuration.filter.SameDayFilterConfiguration;
import com.systematic.trading.backtest.configuration.signals.SignalConfiguration;

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
	private final SignalConfiguration[] signals;

	public EntryLogicConfiguration(final Type type, final PeriodicFilterConfiguration periodic) {
		this.type = type;
		this.periodic = periodic;
		this.confirmationSignal = null;
		this.sameDaySignals = null;
		this.signals = null;
	}

	public EntryLogicConfiguration(final Type type, final ConfirmationSignalFilterConfiguration confirmationSignal,
	        final SignalConfiguration... signals) {
		this.type = type;
		this.periodic = null;
		this.confirmationSignal = confirmationSignal;
		this.sameDaySignals = null;
		this.signals = signals;
	}

	public EntryLogicConfiguration(final Type type, final SameDayFilterConfiguration sameDaySignals,
	        final SignalConfiguration... signals) {
		this.type = type;
		this.periodic = null;
		this.confirmationSignal = null;
		this.sameDaySignals = sameDaySignals;
		this.signals = signals;
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

	public SignalConfiguration[] getSignals() {
		return signals;
	}
}