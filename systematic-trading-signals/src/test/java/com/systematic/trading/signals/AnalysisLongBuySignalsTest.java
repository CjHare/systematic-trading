package com.systematic.trading.signals;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.SignalType;
import com.systematic.trading.signal.IndicatorSignalId;
import com.systematic.trading.signals.indicator.IndicatorSignals;
import com.systematic.trading.signals.model.BuySignal;
import com.systematic.trading.signals.model.filter.AnyIndicatorIsBuySignalFilter;
import com.systematic.trading.signals.model.filter.SignalFilter;
import com.systematic.trading.signals.model.indicator.IndicatorSignal;

@RunWith(MockitoJUnitRunner.class)
public class AnalysisLongBuySignalsTest {

	private static final int ADJUSTMENT = 1;

	@Mock
	private IndicatorSignalId macdId;

	@Mock
	private IndicatorSignalId rsiId;

	@Mock
	private IndicatorSignalId smaId;

	@Mock
	private IndicatorSignals generatorA;

	@Mock
	private IndicatorSignals generatorB;

	/** Trading price data. */
	private TradingDayPrices[] data;

	/** */
	private List<IndicatorSignals> generators;

	/** Signals analysis being tested. */
	private AnalysisBuySignals analysis;

	@Before
	public void setUp() {
		generators = new ArrayList<>();
	}

	@Test(expected = IllegalArgumentException.class)
	public void maximumNumberOfTradingDaysRequiredNullSignals() {
		new AnalysisLongBuySignals(new ArrayList<>(), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void maximumNumberOfTradingDaysRequiredNullGenerators() {
		new AnalysisLongBuySignals(null, new ArrayList<>());
	}

	@Test
	public void maximumNumberOfTradingDaysRequiredEmptyInput() {
		final AnalysisBuySignals analysis = new AnalysisLongBuySignals(new ArrayList<>(), new ArrayList<>());

		assertEquals(0, analysis.getMaximumNumberOfTradingDaysRequired());
	}

	@Test
	public void maximumNumberOfTradingDaysRequiredSingleGenerator() {
		setUpGeneratorTradingDays(generatorA, 10);
		setUpAnalysis();

		assertEquals(10 + ADJUSTMENT, analysis.getMaximumNumberOfTradingDaysRequired());
		verify(generatorA).getRequiredNumberOfTradingDays();
	}

	@Test
	public void maximumNumberOfTradingDaysRequiredMultipleGenerators() {
		setUpGeneratorTradingDays(generatorA, 5);
		setUpGeneratorTradingDays(generatorB, 15);
		setUpAnalysis();

		assertEquals(15 + ADJUSTMENT, analysis.getMaximumNumberOfTradingDaysRequired());
		verify(generatorA).getRequiredNumberOfTradingDays();
		verify(generatorB).getRequiredNumberOfTradingDays();
	}

	@Test
	public void analyzeZeroSignals() {
		setUpGenerator(generatorA, rsiId);
		setUpGenerator(generatorB, macdId);
		setUpAnalysis();

		final List<BuySignal> signals = performAnalysis();

		verifyFilteredSignals(signals);
		verifyGenerator(generatorA);
		verifyGenerator(generatorB);
	}

	@Test
	public void analyze() {
		setUpGenerator(generatorA, rsiId, LocalDate.now().minus(2, ChronoUnit.DAYS));
		setUpGenerator(generatorB, macdId, LocalDate.now().minus(1, ChronoUnit.DAYS), LocalDate.now());

		setUpAnalysisWithFilter();

		final List<BuySignal> signals = performAnalysis();

		verifyFilteredSignals(signals, LocalDate.now().minus(2, ChronoUnit.DAYS),
		        LocalDate.now().minus(1, ChronoUnit.DAYS), LocalDate.now());
		verifyGenerator(generatorA);
		verifyGenerator(generatorB);
	}

	private void verifyFilteredSignals( final List<BuySignal> signals, final LocalDate... signalDates ) {
		assertNotNull(signals);
		assertEquals(signalDates.length, signals.size());

		int index = 0;
		for (final LocalDate signalDate : signalDates) {
			assertEquals(signalDate, signals.get(index).getDate());
			index++;
		}
	}

	private void verifyGenerator( final IndicatorSignals generator ) {
		verify(generator).calculate(data);
		verify(generator).getSignalType();
	}

	private void setUpGenerator( final IndicatorSignals generator, final IndicatorSignalId id,
	        final LocalDate... signalDates ) {
		when(generator.getSignalType()).thenReturn(id);
		final List<IndicatorSignal> signalsGenerator = new ArrayList<>();

		for (final LocalDate singalDate : signalDates) {
			signalsGenerator.add(new IndicatorSignal(singalDate, id, SignalType.BULLISH));
		}

		when(generator.calculate(any(TradingDayPrices[].class))).thenReturn(signalsGenerator);
		generators.add(generator);
	}

	private void setUpGeneratorTradingDays( final IndicatorSignals generator, final int tradingDaysForGenerator ) {
		when(generator.getRequiredNumberOfTradingDays()).thenReturn(tradingDaysForGenerator);
		generators.add(generator);
	}

	private List<BuySignal> performAnalysis() {
		data = new TradingDayPrices[1];
		data[0] = mock(TradingDayPrices.class);
		return analysis.analyse(data);
	}

	private void setUpAnalysis() {
		analysis = new AnalysisLongBuySignals(generators, new ArrayList<>());
	}

	private void setUpAnalysisWithFilter() {
		final List<SignalFilter> filters = new ArrayList<>();
		filters.add(new AnyIndicatorIsBuySignalFilter());
		analysis = new AnalysisLongBuySignals(generators, filters);
	}
}