package com.systematic.trading.backtest.configuration.entry;

import java.util.Optional;

import com.systematic.trading.backtest.configuration.periodic.PeriodicFilterConfiguration;
import com.systematic.trading.backtest.trade.MaximumTrade;
import com.systematic.trading.backtest.trade.MinimumTrade;
import com.systematic.trading.strategy.confirmation.ConfirmationSignalFilterConfiguration;
import com.systematic.trading.strategy.operator.AnyOfIndicatorFilterConfiguration;
import com.systematic.trading.strategy.operator.SameDayFilterConfiguration;

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
	private final Optional<AnyOfIndicatorFilterConfiguration> anyOfSignal;
	private final MaximumTrade maximumTrade;
	private final MinimumTrade minimumTrade;

	public EntryLogicConfiguration( final PeriodicFilterConfiguration periodic, final MaximumTrade maximumTrade,
	        final MinimumTrade minimumTrade ) {
		this.type = Type.PERIODIC;
		this.periodic = periodic;
		this.confirmationSignal = Optional.empty();
		this.sameDaySignals = Optional.empty();
		this.anyOfSignal = Optional.empty();
		this.maximumTrade = maximumTrade;
		this.minimumTrade = minimumTrade;
	}

	public EntryLogicConfiguration( final ConfirmationSignalFilterConfiguration confirmationSignal,
	        final MaximumTrade maximumTrade, final MinimumTrade minimumTrade ) {
		this.type = Type.CONFIRMATION_SIGNAL;
		this.periodic = null;
		this.confirmationSignal = Optional.of(confirmationSignal);
		this.sameDaySignals = Optional.empty();
		this.anyOfSignal = Optional.empty();
		this.maximumTrade = maximumTrade;
		this.minimumTrade = minimumTrade;
	}

	public EntryLogicConfiguration( final SameDayFilterConfiguration sameDaySignals, final MaximumTrade maximumTrade,
	        final MinimumTrade minimumTrade ) {
		this.type = Type.SAME_DAY_SIGNALS;
		this.periodic = null;
		this.confirmationSignal = Optional.empty();
		this.sameDaySignals = Optional.of(sameDaySignals);
		this.anyOfSignal = Optional.empty();
		this.maximumTrade = maximumTrade;
		this.minimumTrade = minimumTrade;
	}

	public EntryLogicConfiguration( final AnyOfIndicatorFilterConfiguration anyOfSignals,
	        final MaximumTrade maximumTrade, final MinimumTrade minimumTrade ) {
		this.type = Type.ANY_SIGNAL;
		this.periodic = null;
		this.confirmationSignal = Optional.empty();
		this.sameDaySignals = Optional.empty();
		this.anyOfSignal = Optional.of(anyOfSignals);
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

	public Optional<AnyOfIndicatorFilterConfiguration> getAnyOfSignal() {
		return anyOfSignal;
	}

	public MaximumTrade getMaximumTrade() {
		return maximumTrade;
	}

	public MinimumTrade getMinimumTrade() {
		return minimumTrade;
	}
}