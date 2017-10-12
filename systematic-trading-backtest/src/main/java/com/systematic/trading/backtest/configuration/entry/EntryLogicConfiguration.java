package com.systematic.trading.backtest.configuration.entry;

import java.util.Optional;

import com.systematic.trading.backtest.configuration.filter.AnyIndicatorFilterConfiguration;
import com.systematic.trading.backtest.configuration.filter.ConfirmationSignalFilterConfiguration;
import com.systematic.trading.backtest.configuration.filter.PeriodicFilterConfiguration;
import com.systematic.trading.backtest.configuration.filter.SameDayFilterConfiguration;
import com.systematic.trading.backtest.trade.MaximumTrade;
import com.systematic.trading.backtest.trade.MinimumTrade;

public class EntryLogicConfiguration {

	public enum Type {
		PERIODIC,
		ANY_SIGNAL,
		CONFIRMATION_SIGNAL,
		SAME_DAY_SIGNALS;
	}

	private final Type type;
	private final PeriodicFilterConfiguration periodic;
	private final Optional<ConfirmationSignalFilterConfiguration> confirmationSignal;
	private final Optional<SameDayFilterConfiguration> sameDaySignals;
	private final Optional<AnyIndicatorFilterConfiguration> anySignal;
	private final MaximumTrade maximumTrade;
	private final MinimumTrade minimumTrade;

	public EntryLogicConfiguration( final PeriodicFilterConfiguration periodic, final MaximumTrade maximumTrade,
	        final MinimumTrade minimumTrade ) {
		this.type = Type.PERIODIC;
		this.periodic = periodic;
		this.confirmationSignal = Optional.empty();
		this.sameDaySignals = Optional.empty();
		this.anySignal = Optional.empty();
		this.maximumTrade = maximumTrade;
		this.minimumTrade = minimumTrade;
	}

	public EntryLogicConfiguration( final ConfirmationSignalFilterConfiguration confirmationSignal,
	        final MaximumTrade maximumTrade, final MinimumTrade minimumTrade ) {
		this.type = Type.CONFIRMATION_SIGNAL;
		this.periodic = null;
		this.confirmationSignal = Optional.of(confirmationSignal);
		this.sameDaySignals = Optional.empty();
		this.anySignal = Optional.empty();
		this.maximumTrade = maximumTrade;
		this.minimumTrade = minimumTrade;
	}

	public EntryLogicConfiguration( final SameDayFilterConfiguration sameDaySignals, final MaximumTrade maximumTrade,
	        final MinimumTrade minimumTrade ) {
		this.type = Type.SAME_DAY_SIGNALS;
		this.periodic = null;
		this.confirmationSignal = Optional.empty();
		this.sameDaySignals = Optional.of(sameDaySignals);
		this.anySignal = Optional.empty();
		this.maximumTrade = maximumTrade;
		this.minimumTrade = minimumTrade;
	}

	public EntryLogicConfiguration( final AnyIndicatorFilterConfiguration signalFilter, final MaximumTrade maximumTrade,
	        final MinimumTrade minimumTrade ) {
		this.type = Type.ANY_SIGNAL;
		this.periodic = null;
		this.confirmationSignal = Optional.empty();
		this.sameDaySignals = Optional.empty();
		this.anySignal = Optional.of(signalFilter);
		this.maximumTrade = maximumTrade;
		this.minimumTrade = minimumTrade;
	}

	public Type getType() {
		return type;
	}

	public PeriodicFilterConfiguration getPeriodic() {
		return periodic;
	}

	public Optional<ConfirmationSignalFilterConfiguration> getConfirmationSignal() {
		return confirmationSignal;
	}

	public Optional<SameDayFilterConfiguration> getSameDaySignals() {
		return sameDaySignals;
	}

	public Optional<AnyIndicatorFilterConfiguration> getAnySignal() {
		return anySignal;
	}

	public MaximumTrade getMaximumTrade() {
		return maximumTrade;
	}

	public MinimumTrade getMinimumTrade() {
		return minimumTrade;
	}
}