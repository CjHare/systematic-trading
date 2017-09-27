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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.maths.SignalType;
import com.systematic.trading.signal.IndicatorSignalId;
import com.systematic.trading.signals.indicator.IndicatorSignal;
import com.systematic.trading.signals.indicator.IndicatorSignalGenerator;
import com.systematic.trading.signals.model.BuySignal;
import com.systematic.trading.signals.model.filter.AnyIndicatorIsBuySignalFilter;
import com.systematic.trading.signals.model.filter.SignalFilter;

@RunWith(MockitoJUnitRunner.class)
public class AnalysisLongBuySignalsTest {

	private static final int ADJUSTMENT = 1;

	@Mock
	private IndicatorSignalId macdId;

	@Mock
	private IndicatorSignalId rsiId;

	@Mock
	private IndicatorSignalId smaId;

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
		final int tradingDaysForGenerator = 10;
		final IndicatorSignalGenerator generator = mock(IndicatorSignalGenerator.class);
		when(generator.getRequiredNumberOfTradingDays()).thenReturn(tradingDaysForGenerator);
		final List<IndicatorSignalGenerator> generators = new ArrayList<>();
		generators.add(generator);

		final AnalysisBuySignals analysis = new AnalysisLongBuySignals(generators, new ArrayList<>());

		assertEquals(tradingDaysForGenerator + ADJUSTMENT, analysis.getMaximumNumberOfTradingDaysRequired());
		verify(generator).getRequiredNumberOfTradingDays();
	}

	@Test
	public void maximumNumberOfTradingDaysRequiredMultipleGenerators() {
		final int tradingDaysForGeneratorA = 5;
		final int tradingDaysForGeneratorB = 15;
		final int tradingDaysForGeneratorC = 52;
		final int tradingDaysForGenerators = tradingDaysForGeneratorC;
		final IndicatorSignalGenerator generatorA = mock(IndicatorSignalGenerator.class);
		final IndicatorSignalGenerator generatorB = mock(IndicatorSignalGenerator.class);
		final IndicatorSignalGenerator generatorC = mock(IndicatorSignalGenerator.class);
		when(generatorA.getRequiredNumberOfTradingDays()).thenReturn(tradingDaysForGeneratorA);
		when(generatorB.getRequiredNumberOfTradingDays()).thenReturn(tradingDaysForGeneratorB);
		when(generatorC.getRequiredNumberOfTradingDays()).thenReturn(tradingDaysForGeneratorC);

		final List<IndicatorSignalGenerator> generators = new ArrayList<>();
		generators.add(generatorA);
		generators.add(generatorB);
		generators.add(generatorC);

		final AnalysisBuySignals analysis = new AnalysisLongBuySignals(generators, new ArrayList<>());

		assertEquals(tradingDaysForGenerators + ADJUSTMENT, analysis.getMaximumNumberOfTradingDaysRequired());
		verify(generatorA).getRequiredNumberOfTradingDays();
		verify(generatorB).getRequiredNumberOfTradingDays();
		verify(generatorC).getRequiredNumberOfTradingDays();
	}

	@Test
	public void analyzeZeroSignals() {

		final IndicatorSignalGenerator generatorA = mock(IndicatorSignalGenerator.class);
		when(generatorA.getSignalType()).thenReturn(rsiId);
		final List<IndicatorSignal> signalsGeneratorA = new ArrayList<>();
		when(generatorA.calculateSignals(any(TradingDayPrices[].class))).thenReturn(signalsGeneratorA);

		final IndicatorSignalGenerator generatorB = mock(IndicatorSignalGenerator.class);
		when(generatorB.getSignalType()).thenReturn(macdId);
		final List<IndicatorSignal> signalsGeneratorB = new ArrayList<>();
		when(generatorA.calculateSignals(any(TradingDayPrices[].class))).thenReturn(signalsGeneratorB);

		final List<IndicatorSignalGenerator> generators = new ArrayList<>();
		generators.add(generatorA);
		generators.add(generatorB);

		final AnalysisBuySignals analysis = new AnalysisLongBuySignals(generators, new ArrayList<>());

		final TradingDayPrices[] data = new TradingDayPrices[1];
		data[0] = mock(TradingDayPrices.class);

		final List<BuySignal> signals = analysis.analyse(data);

		assertNotNull(signals);
		assertEquals(true, signals.isEmpty());
		verify(generatorA).calculateSignals(data);
		verify(generatorA).getSignalType();
		verify(generatorB).calculateSignals(data);
		verify(generatorB).getSignalType();
	}

	@Test
	public void analyze() {

		final IndicatorSignalGenerator generatorA = mock(IndicatorSignalGenerator.class);
		when(generatorA.getSignalType()).thenReturn(rsiId);
		final List<IndicatorSignal> signalsGeneratorA = new ArrayList<>();
		final IndicatorSignal firstSignal = new IndicatorSignal(LocalDate.now().minus(2, ChronoUnit.DAYS), rsiId,
		        SignalType.BULLISH);
		signalsGeneratorA.add(firstSignal);
		when(generatorA.calculateSignals(any(TradingDayPrices[].class))).thenReturn(signalsGeneratorA);

		final IndicatorSignalGenerator generatorB = mock(IndicatorSignalGenerator.class);
		when(generatorB.getSignalType()).thenReturn(macdId);
		final List<IndicatorSignal> signalsGeneratorB = new ArrayList<>();
		final IndicatorSignal secondSignal = new IndicatorSignal(LocalDate.now().minus(1, ChronoUnit.DAYS), macdId,
		        SignalType.BULLISH);
		signalsGeneratorB.add(secondSignal);
		final IndicatorSignal thirdSignal = new IndicatorSignal(LocalDate.now(), smaId, SignalType.BULLISH);
		signalsGeneratorB.add(thirdSignal);
		when(generatorB.calculateSignals(any(TradingDayPrices[].class))).thenReturn(signalsGeneratorB);

		final List<IndicatorSignalGenerator> generators = new ArrayList<>();
		generators.add(generatorA);
		generators.add(generatorB);

		final List<SignalFilter> filters = new ArrayList<>();
		filters.add(new AnyIndicatorIsBuySignalFilter());

		final AnalysisBuySignals analysis = new AnalysisLongBuySignals(generators, filters);

		final TradingDayPrices[] data = new TradingDayPrices[1];
		data[0] = mock(TradingDayPrices.class);

		final List<BuySignal> signals = analysis.analyse(data);

		assertNotNull(signals);
		assertEquals(3, signals.size());
		assertEquals(LocalDate.now().minus(2, ChronoUnit.DAYS), signals.get(0).getDate());
		assertEquals(LocalDate.now().minus(1, ChronoUnit.DAYS), signals.get(1).getDate());
		assertEquals(LocalDate.now(), signals.get(2).getDate());
		verify(generatorA).calculateSignals(data);
		verify(generatorA).getSignalType();
		verify(generatorB).calculateSignals(data);
		verify(generatorB).getSignalType();
	}
}
