package com.systematic.trading.backtest.configuration.entry;

import com.systematic.trading.backtest.configuration.filter.ConfirmationSignalFilterConfiguration;
import com.systematic.trading.backtest.configuration.filter.PeriodicFilterConfiguration;
import com.systematic.trading.backtest.configuration.filter.SameDayFilterConfiguration;
import com.systematic.trading.backtest.configuration.trade.MaximumTrade;
import com.systematic.trading.backtest.configuration.trade.MinimumTrade;

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
	private final MaximumTrade maximumTrade;
	private final MinimumTrade minimumTrade;

	public EntryLogicConfiguration( final PeriodicFilterConfiguration periodic, final MaximumTrade maximumTrade,
	        final MinimumTrade minimumTrade ) {
		this.type = Type.PERIODIC;
		this.periodic = periodic;
		this.confirmationSignal = null;
		this.sameDaySignals = null;
		this.maximumTrade = maximumTrade;
		this.minimumTrade = minimumTrade;
	}

	public EntryLogicConfiguration( final ConfirmationSignalFilterConfiguration confirmationSignal,
	        final MaximumTrade maximumTrade, final MinimumTrade minimumTrade ) {
		this.type = Type.CONFIRMATION_SIGNAL;
		this.periodic = null;
		this.confirmationSignal = confirmationSignal;
		this.sameDaySignals = null;
		this.maximumTrade = maximumTrade;
		this.minimumTrade = minimumTrade;
	}

	public EntryLogicConfiguration( final SameDayFilterConfiguration sameDaySignals, final MaximumTrade maximumTrade,
	        final MinimumTrade minimumTrade ) {
		this.type = Type.SAME_DAY_SIGNALS;
		this.periodic = null;
		this.confirmationSignal = null;
		this.sameDaySignals = sameDaySignals;
		this.maximumTrade = maximumTrade;
		this.minimumTrade = minimumTrade;
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

	public MaximumTrade getMaximumTrade() {
		return maximumTrade;
	}

	public MinimumTrade getMinimumTrade() {
		return minimumTrade;
	}
}