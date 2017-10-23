package com.systematic.trading.signals.model.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

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

import com.systematic.trading.maths.SignalType;
import com.systematic.trading.signal.IndicatorId;
import com.systematic.trading.signals.model.BuySignal;
import com.systematic.trading.signals.model.indicator.IndicatorSignal;

/**
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfirmationIndicatorsSignalFilterTest {

	private static final int DAYS_UNTIL_CONFIRMATION_START = 3;
	private static final int CONFIRMATION_DAY_RANGE = 3;
	private static final LocalDate LATEST_TRADING_DATE = LocalDate.now();

	@Mock
	private IndicatorId anchorSignalType;

	@Mock
	private IndicatorId confirmationSignalType;

	@Mock
	private IndicatorId wrongConfirmationSignalType;

	@Mock
	private Comparator<BuySignal> ordering;

	/** Result signals. */
	private Map<IndicatorId, List<IndicatorSignal>> signals;

	/** Filter instance being tested. */
	private ConfirmationIndicatorsSignalFilter filter;

	@Before
	public void setUp() {
		signals = new HashMap<>();
		signals.put(anchorSignalType, new ArrayList<>());
		signals.put(confirmationSignalType, new ArrayList<>());

		filter = new ConfirmationIndicatorsSignalFilter(ordering, anchorSignalType, confirmationSignalType,
		        DAYS_UNTIL_CONFIRMATION_START, CONFIRMATION_DAY_RANGE);
	}

	@Test
	public void noAnchorSignal() {

		final SortedSet<BuySignal> buySignals = applyFilter();

		verifyNoSignals(buySignals);
	}

	@Test
	public void noAConfirmationSignal() {
		setUpAnchorSignal();

		final SortedSet<BuySignal> buySignals = applyFilter();

		verifyNoSignals(buySignals);
	}

	@Test
	public void wrongAConfirmationSignalWithinnRange() {
		setUpAnchorSignal();
		setUpWrongConfirmationSignal();

		final SortedSet<BuySignal> buySignals = applyFilter();

		verifyNoSignals(buySignals);
	}

	@Test
	public void confirmationSignalOnFirstDayOfRangeNoDelay() {
		setUpAnchorSignal();
		setUpConfirmationSignal(0);
		setUpFilterImmediateStart();

		final SortedSet<BuySignal> buySignals = applyFilter();

		verifySignals(1, buySignals);
	}

	@Test
	public void confirmationSignalOnLastDayOfRangeNoDelay() {
		setUpAnchorSignal();
		setUpConfirmationSignal(CONFIRMATION_DAY_RANGE);
		setUpFilterImmediateStart();

		final SortedSet<BuySignal> buySignals = applyFilter();

		verifySignals(1, buySignals);
	}

	@Test
	public void confirmationSignalOnFirstDayOfRangeTwoDayDelay() {
		setUpAnchorSignal();
		setUpConfirmationSignal(DAYS_UNTIL_CONFIRMATION_START);

		final SortedSet<BuySignal> buySignals = applyFilter();

		verifySignals(1, buySignals);
	}

	@Test
	public void confirmationSignalOnLastDayOfRangeTwoDayDelay() {
		setUpAnchorSignal();
		setUpConfirmationSignal(DAYS_UNTIL_CONFIRMATION_START + CONFIRMATION_DAY_RANGE);

		final SortedSet<BuySignal> buySignals = applyFilter();

		verifySignals(1, buySignals);
	}

	@Test
	public void confirmationSignalTooSoon() {
		setUpAnchorSignal();
		setUpConfirmationSignal(DAYS_UNTIL_CONFIRMATION_START - 1);

		final SortedSet<BuySignal> buySignals = applyFilter();

		verifyNoSignals(buySignals);
	}

	@Test
	public void confirmationSignalTooLate() {
		setUpAnchorSignal();
		setUpConfirmationSignal(DAYS_UNTIL_CONFIRMATION_START + CONFIRMATION_DAY_RANGE + 1);

		final SortedSet<BuySignal> buySignals = applyFilter();

		verifyNoSignals(buySignals);
	}

	@Test
	public void missingAnchorSignalType() {
		createFilterExpectingException("Expecting an anchor IndicatorSignalType", null, confirmationSignalType,
		        DAYS_UNTIL_CONFIRMATION_START, CONFIRMATION_DAY_RANGE);
	}

	@Test
	public void missingConfirmationSignalType() {
		createFilterExpectingException("Expecting an confirmation IndicatorSignalType", anchorSignalType, null,
		        DAYS_UNTIL_CONFIRMATION_START, CONFIRMATION_DAY_RANGE);
	}

	@Test
	public void invalidStartDelay() {
		createFilterExpectingException("Expecting zero or positive number for the days", anchorSignalType,
		        confirmationSignalType, -1, CONFIRMATION_DAY_RANGE);
	}

	@Test
	public void invalidConfirmationRange() {
		createFilterExpectingException("Expecting zero or a positive number of days for the confirmation signal range",
		        anchorSignalType, confirmationSignalType, DAYS_UNTIL_CONFIRMATION_START, -1);
	}

	@Test
	public void missingAnchorSignalsOnApply() {
		clearSignal(anchorSignalType);

		applyExpectingException();
	}

	@Test
	public void missingConfirmationSignalsOnApply() {
		clearSignal(confirmationSignalType);

		applyExpectingException();
	}

	private void setUpConfirmationSignal( final int daysFromNow ) {
		setUpConfirmationSignal(new IndicatorSignal(LocalDate.now().plus(daysFromNow, ChronoUnit.DAYS),
		        confirmationSignalType, SignalType.BULLISH));
	}

	private void setUpFilterImmediateStart() {
		filter = new ConfirmationIndicatorsSignalFilter(ordering, anchorSignalType, confirmationSignalType, 0,
		        CONFIRMATION_DAY_RANGE);
	}

	private void createFilterExpectingException( final String expectedMessage, final IndicatorId anchor,
	        final IndicatorId confirmation, final int delayUntilConfirmationRange,
	        final int confirmationDayRange ) {
		try {
			new ConfirmationIndicatorsSignalFilter(ordering, anchor, confirmation, delayUntilConfirmationRange,
			        confirmationDayRange);
			fail("Expecting exception");
		} catch (final IllegalArgumentException e) {
			assertEquals(expectedMessage, e.getMessage());
		}

		verifyZeroInteractions(ordering);
	}

	private void clearSignal( IndicatorId id ) {
		signals.put(id, null);
	}

	private void applyExpectingException() {
		try {
			applyFilter();
			fail("Expecting exception");
		} catch (final IllegalArgumentException e) {
			assertEquals("Expecting a non-null entries for types anchorSignalType and confirmationSignalType",
			        e.getMessage());
		}

		verifyZeroInteractions(ordering);
	}

	private SortedSet<BuySignal> applyFilter() {
		return filter.apply(signals, LATEST_TRADING_DATE);
	}

	private void verifySignals( final int expected, final SortedSet<BuySignal> filteredSignals ) {
		assertNotNull(filteredSignals);
		assertEquals(expected, filteredSignals.size());
		verify(ordering, times(expected)).compare(any(BuySignal.class), any(BuySignal.class));
		verifyNoMoreInteractions(ordering);
	}

	private void verifyNoSignals( final SortedSet<BuySignal> filteredSignals ) {
		assertNotNull(filteredSignals);
		assertEquals(0, filteredSignals.size());
		verifyZeroInteractions(ordering);
	}

	private void setUpAnchorSignal() {
		final List<IndicatorSignal> anchorSignals = new ArrayList<>();
		anchorSignals.add(new IndicatorSignal(LocalDate.now(), anchorSignalType, SignalType.BULLISH));
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
		        .add(new IndicatorSignal(LocalDate.now(), wrongConfirmationSignalType, SignalType.BULLISH));
		signals.put(wrongConfirmationSignalType, wrongConfirmationSignals);
	}
}