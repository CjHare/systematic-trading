package com.systematic.trading.signals.model.filter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.signals.indicator.IndicatorSignal;
import com.systematic.trading.signals.model.BuySignal;
import com.systematic.trading.signals.model.IndicatorDirectionType;
import com.systematic.trading.signals.model.IndicatorSignalType;

@RunWith(MockitoJUnitRunner.class)
public class ConfirmationIndicatorsSignalFilterTest {

	private final IndicatorSignalType anchorSignalType = IndicatorSignalType.RSI;
	private final IndicatorSignalType confirmationSignalType = IndicatorSignalType.MACD;
	private final IndicatorSignalType wrongConfirmationSignalType = IndicatorSignalType.SMA;
	private final int daysUntilStartOfConfirmationRange = 3;
	private final int confirmationDayRange = 3;
	private final LocalDate latestTradingDate = LocalDate.now();

	@Mock
	private Comparator<BuySignal> ordering;

	private Map<IndicatorSignalType, List<IndicatorSignal>> signals;
	private ConfirmationIndicatorsSignalFilter filter;

	@Before
	public void setUp() {
		signals = new HashMap<>();
		signals.put(anchorSignalType, new ArrayList<>());
		signals.put(confirmationSignalType, new ArrayList<>());

		filter = new ConfirmationIndicatorsSignalFilter(anchorSignalType, confirmationSignalType,
		        daysUntilStartOfConfirmationRange, confirmationDayRange);
	}

	private void setUpAnchorSignal() {
		final List<IndicatorSignal> anchorSignals = new ArrayList<>();
		anchorSignals.add(new IndicatorSignal(LocalDate.now(), anchorSignalType, IndicatorDirectionType.BULLISH));
		signals.put(anchorSignalType, anchorSignals);
	}

	private void setUpConfirmationSignal( final IndicatorSignal signal ) {
		final List<IndicatorSignal> confirmationSignals = new ArrayList<>();
		confirmationSignals.add(signal);
		signals.put(confirmationSignalType, confirmationSignals);
	}

	private void setUpWrongConfirmationSignal() {
		final List<IndicatorSignal> wrongConfirmationSignals = new ArrayList<>();
		wrongConfirmationSignals
		        .add(new IndicatorSignal(LocalDate.now(), wrongConfirmationSignalType, IndicatorDirectionType.BULLISH));
		signals.put(wrongConfirmationSignalType, wrongConfirmationSignals);
	}

	@Test
	public void missingAnchorSignalType() {
		try {
			new ConfirmationIndicatorsSignalFilter(null, confirmationSignalType, daysUntilStartOfConfirmationRange,
			        confirmationDayRange);
			fail("Expecting exception");
		} catch (final IllegalArgumentException e) {
			assertEquals("Expecting an anchor IndicatorSignalType", e.getMessage());
		}

		verifyZeroInteractions(ordering);
	}

	@Test
	public void missingConfirmationSignalType() {
		try {
			new ConfirmationIndicatorsSignalFilter(anchorSignalType, null, daysUntilStartOfConfirmationRange,
			        confirmationDayRange);
			fail("Expecting exception");
		} catch (final IllegalArgumentException e) {
			assertEquals("Expecting an anchor IndicatorSignalType", e.getMessage());
		}

		verifyZeroInteractions(ordering);
	}

	@Test
	public void invalidStartDelay() {
		try {
			new ConfirmationIndicatorsSignalFilter(anchorSignalType, confirmationSignalType, -1, confirmationDayRange);
			fail("Expecting exception");
		} catch (final IllegalArgumentException e) {
			assertEquals("Expecting zero or positive number for the days", e.getMessage());
		}

		verifyZeroInteractions(ordering);
	}

	@Test
	public void invalidConfirmationRange() {
		try {
			new ConfirmationIndicatorsSignalFilter(anchorSignalType, confirmationSignalType,
			        daysUntilStartOfConfirmationRange, -1);
			fail("Expecting exception");
		} catch (final IllegalArgumentException e) {
			assertEquals("Expecting zero or a positive number of days for the confirmation signal range",
			        e.getMessage());
		}

		verifyZeroInteractions(ordering);
	}

	@Test
	public void noAnchorSignal() {
		final SortedSet<BuySignal> buySignals = filter.apply(signals, ordering, latestTradingDate);

		assertNotNull(buySignals);
		assertEquals(0, buySignals.size());
		verifyZeroInteractions(ordering);
	}

	@Test
	public void noAConfirmationSignal() {
		setUpAnchorSignal();

		final SortedSet<BuySignal> buySignals = filter.apply(signals, ordering, latestTradingDate);

		assertNotNull(buySignals);
		assertEquals(0, buySignals.size());
		verifyZeroInteractions(ordering);
	}

	@Test
	public void wrongAConfirmationSignalWithinnRange() {
		setUpAnchorSignal();
		setUpWrongConfirmationSignal();

		final SortedSet<BuySignal> buySignals = filter.apply(signals, ordering, latestTradingDate);

		assertNotNull(buySignals);
		assertEquals(0, buySignals.size());
		verifyZeroInteractions(ordering);
	}

	@Test
	public void confirmationSignalOnFirstDayOfRangeNoDelay() {
		setUpAnchorSignal();
		setUpConfirmationSignal(new IndicatorSignal(LocalDate.now().plus(0, ChronoUnit.DAYS), confirmationSignalType,
		        IndicatorDirectionType.BULLISH));
		filter = new ConfirmationIndicatorsSignalFilter(anchorSignalType, confirmationSignalType, 0,
		        confirmationDayRange);

		final SortedSet<BuySignal> buySignals = filter.apply(signals, ordering, latestTradingDate);

		assertNotNull(buySignals);
		assertEquals(1, buySignals.size());
		verify(ordering).compare(any(BuySignal.class), any(BuySignal.class));
	}

	@Test
	public void confirmationSignalOnLastDayOfRangeNoDelay() {
		setUpAnchorSignal();
		setUpConfirmationSignal(new IndicatorSignal(LocalDate.now().plus(confirmationDayRange, ChronoUnit.DAYS),
		        confirmationSignalType, IndicatorDirectionType.BULLISH));
		filter = new ConfirmationIndicatorsSignalFilter(anchorSignalType, confirmationSignalType, 0,
		        confirmationDayRange);

		final SortedSet<BuySignal> buySignals = filter.apply(signals, ordering, latestTradingDate);

		assertNotNull(buySignals);
		assertEquals(1, buySignals.size());
		verify(ordering).compare(any(BuySignal.class), any(BuySignal.class));
	}

	@Test
	public void confirmationSignalOnFirstDayOfRangeTwoDayDelay() {
		setUpAnchorSignal();
		setUpConfirmationSignal(
		        new IndicatorSignal(LocalDate.now().plus(daysUntilStartOfConfirmationRange, ChronoUnit.DAYS),
		                confirmationSignalType, IndicatorDirectionType.BULLISH));

		final SortedSet<BuySignal> buySignals = filter.apply(signals, ordering, latestTradingDate);

		assertNotNull(buySignals);
		assertEquals(1, buySignals.size());
		verify(ordering).compare(any(BuySignal.class), any(BuySignal.class));
	}

	@Test
	public void confirmationSignalOnLastDayOfRangeTwoDayDelay() {
		setUpAnchorSignal();
		setUpConfirmationSignal(
		        new IndicatorSignal(
		                LocalDate.now().plus(daysUntilStartOfConfirmationRange, ChronoUnit.DAYS)
		                        .plus(confirmationDayRange, ChronoUnit.DAYS),
		                confirmationSignalType, IndicatorDirectionType.BULLISH));

		final SortedSet<BuySignal> buySignals = filter.apply(signals, ordering, latestTradingDate);

		assertNotNull(buySignals);
		assertEquals(1, buySignals.size());
		verify(ordering).compare(any(BuySignal.class), any(BuySignal.class));
	}

	@Test
	public void confirmationSignalTooSoon() {
		setUpAnchorSignal();
		setUpConfirmationSignal(
		        new IndicatorSignal(LocalDate.now().plus(daysUntilStartOfConfirmationRange - 1, ChronoUnit.DAYS),
		                confirmationSignalType, IndicatorDirectionType.BULLISH));

		final SortedSet<BuySignal> buySignals = filter.apply(signals, ordering, latestTradingDate);

		assertNotNull(buySignals);
		assertEquals(0, buySignals.size());
		verifyZeroInteractions(ordering);
	}

	@Test
	public void confirmationSignalTooLate() {
		setUpAnchorSignal();
		setUpConfirmationSignal(new IndicatorSignal(
		        LocalDate.now().plus(daysUntilStartOfConfirmationRange + confirmationDayRange + 1, ChronoUnit.DAYS),
		        confirmationSignalType, IndicatorDirectionType.BULLISH));

		final SortedSet<BuySignal> buySignals = filter.apply(signals, ordering, latestTradingDate);

		assertNotNull(buySignals);
		assertEquals(0, buySignals.size());
		verifyZeroInteractions(ordering);
	}

	@Test
	public void missingAnchorSignalsOnApply() {
		signals.put(anchorSignalType, null);

		try {
			filter.apply(signals, ordering, latestTradingDate);
			fail("Expecting exception");
		} catch (final IllegalArgumentException e) {
			assertEquals("Expecting a non-null entries for types RSI and MACD", e.getMessage());
		}

		verifyZeroInteractions(ordering);
	}

	@Test
	public void missingConfirmationSignalsOnApply() {
		signals.put(confirmationSignalType, null);

		try {
			filter.apply(signals, ordering, latestTradingDate);
			fail("Expecting exception");
		} catch (final IllegalArgumentException e) {
			assertEquals("Expecting a non-null entries for types RSI and MACD", e.getMessage());
		}

		verifyZeroInteractions(ordering);
	}
}