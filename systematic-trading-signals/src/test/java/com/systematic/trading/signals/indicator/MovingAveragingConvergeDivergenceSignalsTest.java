package com.systematic.trading.signals.indicator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.math.MathContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.signals.filter.SignalRangeFilter;
import com.systematic.trading.signals.model.IndicatorDirectionType;
import com.systematic.trading.signals.model.IndicatorSignalType;

@RunWith(MockitoJUnitRunner.class)
public class MovingAveragingConvergeDivergenceSignalsTest extends SignalTest {

	private static final int FAST_TIME_PERIODS = 10;

	@Mock
	private SignalRangeFilter filter;

	@Before
	public void setUp() {
		when(filter.getEarliestSignalDate(any(TradingDayPrices[].class))).thenReturn(getStartDate(FAST_TIME_PERIODS));
	}

	@Test
	public void getRequiredNumberOfTradingDays() {
		final int slowTimePeriods = 20;
		final int signalTimePeriods = 7;

		final MovingAveragingConvergeDivergenceSignals signals = new MovingAveragingConvergeDivergenceSignals(
		        FAST_TIME_PERIODS, slowTimePeriods, signalTimePeriods, filter, MATH_CONTEXT);

		assertEquals(37, signals.getRequiredNumberOfTradingDays());
	}

	@Test
	public void getSignalType() {
		final MovingAveragingConvergeDivergenceSignals signals = new MovingAveragingConvergeDivergenceSignals(10, 20, 7,
		        filter, MathContext.DECIMAL64);

		assertEquals(IndicatorSignalType.MACD, signals.getSignalType());
	}

	@Test
	public void calculateSignalsFlatline() {
		final int slowTimePeriods = 20;
		final int signalTimePeriods = 7;

		final MovingAveragingConvergeDivergenceSignals signals = new MovingAveragingConvergeDivergenceSignals(
		        FAST_TIME_PERIODS, slowTimePeriods, signalTimePeriods, filter, MATH_CONTEXT);

		final TradingDayPrices[] data = createFlatTradingDayPrices(37, 10);

		final List<IndicatorSignal> results = signals.calculateSignals(data);

		assertNotNull(results);
		assertEquals(0, results.size());
	}

	@Test
	public void calculateOneSignal() {
		final int buyPriceSpike = 7;
		final int slowTimePeriods = 20;
		final int signalTimePeriods = 7;

		final MovingAveragingConvergeDivergenceSignals signals = new MovingAveragingConvergeDivergenceSignals(
		        FAST_TIME_PERIODS, slowTimePeriods, signalTimePeriods, filter, MATH_CONTEXT);

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