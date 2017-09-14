package com.systematic.trading.signals.indicator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.MathContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.Test;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.signals.model.IndicatorDirectionType;
import com.systematic.trading.signals.model.IndicatorSignalType;

public class MovingAveragingConvergeDivergenceSignalsTest extends SignalTest {

	private static final int SIGNAL_WITHIN_LAST_WEEK = 7;

	@Test
	public void getRequiredNumberOfTradingDays() {
		final int fastTimePeriods = 10;
		final int slowTimePeriods = 20;
		final int signalTimePeriods = 7;

		final MovingAveragingConvergeDivergenceSignals signals = new MovingAveragingConvergeDivergenceSignals(
		        fastTimePeriods, slowTimePeriods, signalTimePeriods, SIGNAL_WITHIN_LAST_WEEK, MATH_CONTEXT);

		assertEquals(37, signals.getRequiredNumberOfTradingDays());
	}

	@Test
	public void getSignalType() {
		final MovingAveragingConvergeDivergenceSignals signals = new MovingAveragingConvergeDivergenceSignals(10, 20, 7,
		        SIGNAL_WITHIN_LAST_WEEK, MathContext.DECIMAL64);

		assertEquals(IndicatorSignalType.MACD, signals.getSignalType());
	}

	@Test
	public void calculateSignalsFlatline() {
		final int fastTimePeriods = 10;
		final int slowTimePeriods = 20;
		final int signalTimePeriods = 7;

		final MovingAveragingConvergeDivergenceSignals signals = new MovingAveragingConvergeDivergenceSignals(
		        fastTimePeriods, slowTimePeriods, signalTimePeriods, SIGNAL_WITHIN_LAST_WEEK, MATH_CONTEXT);

		final TradingDayPrices[] data = createFlatTradingDayPrices(37, 10);

		final List<IndicatorSignal> results = signals.calculateSignals(data);

		assertNotNull(results);
		assertEquals(0, results.size());
	}

	@Test
	public void calculateOneSignals() {
		final int buyPriceSpike = 7;
		final int fastTimePeriods = 10;
		final int slowTimePeriods = 20;
		final int signalTimePeriods = 7;

		final MovingAveragingConvergeDivergenceSignals signals = new MovingAveragingConvergeDivergenceSignals(
		        fastTimePeriods, slowTimePeriods, signalTimePeriods, SIGNAL_WITHIN_LAST_WEEK, MATH_CONTEXT);

		// Create a down, then an up-spike
		final TradingDayPrices[] data = addStep(15, 15, -100,
		        addStep(30, buyPriceSpike, 100, createFlatTradingDayPrices(37, 25)));

		final List<IndicatorSignal> results = signals.calculateSignals(data);

		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(IndicatorSignalType.MACD, results.get(0).getSignal());
		assertEquals(IndicatorDirectionType.BULLISH, results.get(0).getDirection());

		assertEquals(LocalDate.now().minus(buyPriceSpike, ChronoUnit.DAYS), results.get(0).getDate());
	}

	//TODO add tests for bullish signal generation from MACD lines
}