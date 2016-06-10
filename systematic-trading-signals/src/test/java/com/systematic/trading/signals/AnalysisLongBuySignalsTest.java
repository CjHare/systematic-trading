package com.systematic.trading.signals;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.systematic.trading.signals.indicator.IndicatorSignalGenerator;

public class AnalysisLongBuySignalsTest {

	private static final int ADJUSTMENT = 1;

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
		final AnalysisBuySignals signals = new AnalysisLongBuySignals(new ArrayList<>(), new ArrayList<>());

		assertEquals(0, signals.getMaximumNumberOfTradingDaysRequired());
	}

	@Test
	public void maximumNumberOfTradingDaysRequiredSingleGenerator() {
		final int tradingDaysForGenerator = 10;
		final IndicatorSignalGenerator generator = mock(IndicatorSignalGenerator.class);
		when(generator.getRequiredNumberOfTradingDays()).thenReturn(tradingDaysForGenerator);
		final List<IndicatorSignalGenerator> generators = new ArrayList<>();
		generators.add(generator);

		final AnalysisBuySignals signals = new AnalysisLongBuySignals(generators, new ArrayList<>());

		assertEquals(tradingDaysForGenerator + ADJUSTMENT, signals.getMaximumNumberOfTradingDaysRequired());
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

		final AnalysisBuySignals signals = new AnalysisLongBuySignals(generators, new ArrayList<>());

		assertEquals(tradingDaysForGenerators + ADJUSTMENT, signals.getMaximumNumberOfTradingDaysRequired());
		verify(generatorA).getRequiredNumberOfTradingDays();
		verify(generatorB).getRequiredNumberOfTradingDays();
		verify(generatorC).getRequiredNumberOfTradingDays();
	}
	
	@Test
	public void analyze(){
		//TODO code
		fail("Code this test");
		
		
		//TODO verify multiple signals filters are called
		
	}
}
