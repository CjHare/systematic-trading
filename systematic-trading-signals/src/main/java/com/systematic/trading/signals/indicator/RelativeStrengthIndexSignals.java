package com.systematic.trading.signals.indicator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.formula.rs.RelativeStrengthCalculator;
import com.systematic.trading.maths.indicator.IllegalArgumentThrowingValidator;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndex;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndexCalculator;
import com.systematic.trading.maths.indicator.rsi.RelativeStrengthIndexDataPoint;
import com.systematic.trading.signals.model.IndicatorSignalType;

public class RelativeStrengthIndexSignals implements IndicatorSignalGenerator {

	/** The least number of data points that enables RSI signal generation. */
	private static final int MINIMUM_DAYS_OF_RSI_VALUES = 2;

	private final RelativeStrengthIndex rsi;

	/** Required number of data points required for RSI calculation. */
	private final int minimumNumberOfPrices;

	/** Threshold for when the RSI is considered as over sold.*/
	private final BigDecimal oversold;

	/** Threshold for when the RSI is considered as over brought.*/
	private final BigDecimal overbrought;

	/**
	 * @param lookback the number of data points to use in calculations.
	 * @param daysOfRsiValues the number of RSI values desired.
	 */
	public RelativeStrengthIndexSignals(final int lookback, final BigDecimal oversold, final BigDecimal overbrought,
	        final MathContext mathContext) {
		this.minimumNumberOfPrices = lookback + MINIMUM_DAYS_OF_RSI_VALUES;
		this.overbrought = overbrought;
		this.oversold = oversold;
		this.rsi = new RelativeStrengthIndexCalculator(
		        new RelativeStrengthCalculator(lookback, MINIMUM_DAYS_OF_RSI_VALUES,
		                new IllegalArgumentThrowingValidator(), mathContext),
		        new IllegalArgumentThrowingValidator(), mathContext);
	}

	@Override
	public int getRequiredNumberOfTradingDays() {
		return minimumNumberOfPrices;
	}

	@Override
	public List<IndicatorSignal> calculateSignals( final TradingDayPrices[] data ) {

		//TODO write tests

		//TODO validate the number of data items meets the minimum

		//TODO need a consistent return type from all the calculators, dated values (tuples)

		final List<RelativeStrengthIndexDataPoint> rsiData = rsi.rsi(data);

		List<IndicatorSignal> signals = new ArrayList<>();
		signals = addBuySignals(rsiData, signals);
		signals = addSellSignals(rsiData, signals);
		return signals;
	}

	@Override
	public IndicatorSignalType getSignalType() {
		return IndicatorSignalType.RSI;
	}

	private List<IndicatorSignal> addBuySignals( final List<RelativeStrengthIndexDataPoint> rsiData,
	        final List<IndicatorSignal> signals ) {

		//TODO code

		return signals;
	}

	private List<IndicatorSignal> addSellSignals( final List<RelativeStrengthIndexDataPoint> rsiData,
	        final List<IndicatorSignal> signals ) {

		//TODO code

		return signals;
	}

}
