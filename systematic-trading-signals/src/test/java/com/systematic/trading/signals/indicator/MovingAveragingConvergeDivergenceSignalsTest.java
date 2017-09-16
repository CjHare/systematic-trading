package com.systematic.trading.signals.indicator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.indicator.macd.MovingAverageConvergenceDivergence;
import com.systematic.trading.maths.indicator.macd.MovingAverageConvergenceDivergenceLines;
import com.systematic.trading.signals.filter.SignalRangeFilter;
import com.systematic.trading.signals.model.IndicatorSignalType;

@RunWith(MockitoJUnitRunner.class)
public class MovingAveragingConvergeDivergenceSignalsTest {

	private static final int REQUIRED_TRADING_DAYS = 34;

	@Mock
	private SignalCalculator<MovingAverageConvergenceDivergenceLines> firstCalculator;

	@Mock
	private SignalCalculator<MovingAverageConvergenceDivergenceLines> secondCalculator;

	@Mock
	private SignalRangeFilter filter;

	@Mock
	private MovingAverageConvergenceDivergence macd;

	@Mock
	private MovingAverageConvergenceDivergenceLines lines;

	private List<SignalCalculator<MovingAverageConvergenceDivergenceLines>> signalCalculators;

	private TradingDayPrices[] data;

	@Before
	public void setUp() {
		when(macd.macd(any(TradingDayPrices[].class))).thenReturn(lines);

		signalCalculators = new ArrayList<>();
		signalCalculators.add(firstCalculator);
		signalCalculators.add(secondCalculator);

		data = new TradingDayPrices[0];

	}

	@Test
	public void getRequiredNumberOfTradingDays() {
		final MovingAveragingConvergenceDivergenceSignals macdSignals = setUpSignals();

		assertEquals(REQUIRED_TRADING_DAYS, macdSignals.getRequiredNumberOfTradingDays());
	}

	@Test
	public void getSignalType() {
		final MovingAveragingConvergenceDivergenceSignals macdSignals = setUpSignals();

		assertEquals(IndicatorSignalType.MACD, macdSignals.getSignalType());
	}

	@Test
	public void noSignalCalculators() {
		removeSignalCalculators();
		final MovingAveragingConvergenceDivergenceSignals macdSignals = setUpSignals();

		final List<IndicatorSignal> signals = macdSignals.calculateSignals(data);

		verifySignals(signals);
		verifyMacdCaclculator();
	}

	@Test
	public void twoSignalCalculatorsNoSignals() {
		final MovingAveragingConvergenceDivergenceSignals macdSignals = setUpSignals();

		final List<IndicatorSignal> signals = macdSignals.calculateSignals(data);

		verifySignals(signals);
		verifyMacdCaclculator();
		verifyFirstCalculatorSignals();
		verifySecondCalculatorSignals();
	}

	private void verifySignals( final List<IndicatorSignal> signals ) {
		assertNotNull(signals);
	}

	private void verifyMacdCaclculator() {
		verify(macd).macd(data);
		verifyNoMoreInteractions(macd);
	}

	@SuppressWarnings("unchecked")
	private void verifyFirstCalculatorSignals() {
		verify(firstCalculator).calculateSignals(eq(lines), any(Predicate.class));
		verifyNoMoreInteractions(firstCalculator);
	}

	@SuppressWarnings("unchecked")
	private void verifySecondCalculatorSignals() {
		verify(secondCalculator).calculateSignals(eq(lines), any(Predicate.class));
		verifyNoMoreInteractions(secondCalculator);
	}

	private MovingAveragingConvergenceDivergenceSignals setUpSignals() {
		return new MovingAveragingConvergenceDivergenceSignals(macd, REQUIRED_TRADING_DAYS, signalCalculators, filter,
		        MathContext.DECIMAL64);
	}

	private void removeSignalCalculators() {
		signalCalculators.clear();
	}
}