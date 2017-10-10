package com.systematic.trading.signals.indicator.macd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.SignalType;
import com.systematic.trading.maths.indicator.macd.MovingAverageConvergenceDivergence;
import com.systematic.trading.maths.indicator.macd.MovingAverageConvergenceDivergenceLines;
import com.systematic.trading.signal.IndicatorSignalId;
import com.systematic.trading.signals.filter.SignalRangeFilter;
import com.systematic.trading.signals.indicator.IndicatorSignal;
import com.systematic.trading.signals.indicator.GenericIndicatorSignals;
import com.systematic.trading.signals.indicator.SignalCalculator;
import com.systematic.trading.signals.model.DatedSignal;

@RunWith(MockitoJUnitRunner.class)
public class MovingAveragingConvergeDivergenceSignalsTest {

	//TODO generic test behaviour - refactor
	
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

	@Mock
	private IndicatorSignalId macdId;

	private List<SignalCalculator<MovingAverageConvergenceDivergenceLines>> signalCalculators;

	private TradingDayPrices[] data;

	/** Indicator instance being tested. */
	private GenericIndicatorSignals<MovingAverageConvergenceDivergenceLines, MovingAverageConvergenceDivergence> indicator;

	@Before
	public void setUp() {
		when(macd.calculate(any(TradingDayPrices[].class))).thenReturn(lines);

		signalCalculators = new ArrayList<>();
		signalCalculators.add(firstCalculator);
		signalCalculators.add(secondCalculator);

		data = new TradingDayPrices[0];

		setUpMacdSignals();
	}

	@Test
	public void getRequiredNumberOfTradingDays() {
		assertEquals(REQUIRED_TRADING_DAYS, indicator.getRequiredNumberOfTradingDays());
	}

	@Test
	public void getSignalType() {
		assertEquals(macdId, indicator.getSignalType());
	}

	@Test
	public void noSignalCalculators() {
		removeSignalCalculators();
		setUpMacdSignals();

		final List<IndicatorSignal> signals = macd();

		verifySignals(signals);
		verifyMacdCaclculation();
	}

	@Test
	public void twoSignalCalculatorsNoSignals() {

		final List<IndicatorSignal> signals = macd();

		verifySignals(signals);
		verifyMacdCaclculation();
		verifyFirstCalculatorSignals();
		verifySecondCalculatorSignals();
	}

	@Test
	public void firstSignalCalculatorTwoSignals() {
		final DatedSignal firstSignal = new DatedSignal(LocalDate.ofEpochDay(1), SignalType.BULLISH);
		final DatedSignal secondSignal = new DatedSignal(LocalDate.ofEpochDay(5), SignalType.BULLISH);
		setUpCalculator(firstCalculator, firstSignal, secondSignal);
		setUpMacdSignals();

		final List<IndicatorSignal> signals = macd();

		verifySignals(signals, firstSignal, secondSignal);
		verifyMacdCaclculation();
		verifyFirstCalculatorSignals(2);
		verifySecondCalculatorSignals();
	}

	@Test
	public void secondSignalCalculatorTwoSignals() {
		final DatedSignal firstSignal = new DatedSignal(LocalDate.ofEpochDay(1), SignalType.BULLISH);
		final DatedSignal secondSignal = new DatedSignal(LocalDate.ofEpochDay(5), SignalType.BULLISH);
		setUpCalculator(secondCalculator, firstSignal, secondSignal);
		setUpMacdSignals();

		final List<IndicatorSignal> signals = macd();

		verifySignals(signals, firstSignal, secondSignal);
		verifyMacdCaclculation();
		verifyFirstCalculatorSignals();
		verifySecondCalculatorSignals(2);
	}

	@Test
	public void eachSignalCalculatorOneSignal() {
		final DatedSignal firstSignal = new DatedSignal(LocalDate.ofEpochDay(1), SignalType.BULLISH);
		final DatedSignal secondSignal = new DatedSignal(LocalDate.ofEpochDay(5), SignalType.BULLISH);
		setUpCalculator(firstCalculator, secondSignal);
		setUpCalculator(secondCalculator, firstSignal);
		setUpMacdSignals();

		final List<IndicatorSignal> signals = macd();

		verifySignals(signals, secondSignal, firstSignal);
		verifyMacdCaclculation();
		verifyFirstCalculatorSignals(1);
		verifySecondCalculatorSignals(1);
	}

	@SuppressWarnings("unchecked")
	private void setUpCalculator( SignalCalculator<MovingAverageConvergenceDivergenceLines> calculator,
	        final DatedSignal... signals ) {
		final List<DatedSignal> datedSignals = new ArrayList<>();
		for (final DatedSignal signal : signals) {
			datedSignals.add(signal);
		}

		when(calculator.calculateSignals(any(MovingAverageConvergenceDivergenceLines.class), any(Predicate.class)))
		        .thenReturn(datedSignals);
	}

	private List<IndicatorSignal> macd() {
		return indicator.calculate(data);
	}

	private void verifySignals( final List<IndicatorSignal> indicatorSignals, final DatedSignal... datedSignals ) {
		assertNotNull(indicatorSignals);

		if (datedSignals.length > 0) {
			assertEquals("Expecting the same number of indicator as dated signals", datedSignals.length,
			        indicatorSignals.size());

			for (int i = 0; i < datedSignals.length; i++) {
				assertEquals(datedSignals[i].getDate(), indicatorSignals.get(i).getDate());
			}
		}
	}

	private void verifyMacdCaclculation() {
		verify(macd).calculate(data);
		verifyNoMoreInteractions(macd);
	}

	private void verifyFirstCalculatorSignals( final int... typeCount ) {
		verifyCalculatorSignals(firstCalculator, typeCount.length == 0 ? 0 : typeCount[0]);
	}

	private void verifySecondCalculatorSignals( final int... typeCount ) {
		verifyCalculatorSignals(secondCalculator, typeCount.length == 0 ? 0 : typeCount[0]);
	}

	@SuppressWarnings("unchecked")
	private void verifyCalculatorSignals( SignalCalculator<MovingAverageConvergenceDivergenceLines> calculator,
	        final int typeCount ) {
		verify(calculator).calculateSignals(eq(lines), any(Predicate.class));
		verify(calculator, times(typeCount)).getType();
		verifyNoMoreInteractions(calculator);
	}

	private void setUpMacdSignals() {
		indicator = new GenericIndicatorSignals<MovingAverageConvergenceDivergenceLines, MovingAverageConvergenceDivergence>(
		        macdId, macd, REQUIRED_TRADING_DAYS, signalCalculators, filter);
	}

	private void removeSignalCalculators() {
		signalCalculators.clear();
	}
}