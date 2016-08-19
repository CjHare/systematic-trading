package com.systematic.trading.backtest.configuration.filter;

import com.systematic.trading.backtest.configuration.signals.SignalConfiguration;

public class SameDayFilterConfiguration {

	public enum Type {
		ALL;
	}

	private final SignalConfiguration[] signals;
	private final Type type;

	public SameDayFilterConfiguration(final Type type, final SignalConfiguration... signals) {
		this.signals = signals;
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public SignalConfiguration[] getSignals() {
		return signals;
	}
}