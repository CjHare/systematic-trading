/**
 * Copyright (c) 2015, CJ Hare All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 *
 * * Neither the name of [project] nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.systematic.trading.backtest;

import java.math.MathContext;
import java.time.LocalDate;
import java.time.Period;

import com.systematic.trading.backtest.analysis.impl.CulmativeReturnOnInvestmentCalculator;
import com.systematic.trading.backtest.analysis.impl.PeriodicCulmativeReturnOnInvestmentCalculatorListener;
import com.systematic.trading.data.DataService;
import com.systematic.trading.data.DataServiceImpl;
import com.systematic.trading.data.DataServiceUpdater;
import com.systematic.trading.data.DataServiceUpdaterImpl;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.event.EventListener;
import com.systematic.trading.signals.indicator.MovingAveragingConvergeDivergenceSignals;
import com.systematic.trading.signals.indicator.RelativeStrengthIndexSignals;
import com.systematic.trading.signals.indicator.StochasticOscillatorSignals;
import com.systematic.trading.signals.model.configuration.AllSignalsConfiguration;
import com.systematic.trading.signals.model.configuration.LongBuySignalConfiguration;

/**
 * Common code between the back test main classes.
 * 
 * @author CJ Hare
 */
public class BacktestCommon {

	public static LongBuySignalConfiguration getStandardSignalConfiguration() {
		final RelativeStrengthIndexSignals rsi = new RelativeStrengthIndexSignals( 70, 30 );
		final MovingAveragingConvergeDivergenceSignals macd = new MovingAveragingConvergeDivergenceSignals( 10, 20, 7 );
		final StochasticOscillatorSignals stochastic = new StochasticOscillatorSignals( 10, 3, 3 );
		return new AllSignalsConfiguration( rsi, macd, stochastic );
	}

	public static TradingDayPrices[] getTradingData( final String tickerSymbol, final LocalDate startDate,
			final LocalDate endDate ) {

		final DataServiceUpdater updateService = DataServiceUpdaterImpl.getInstance();
		updateService.get( tickerSymbol, startDate, endDate );

		final DataService service = DataServiceImpl.getInstance();
		return service.get( tickerSymbol, startDate, endDate );
	}

	public static CulmativeReturnOnInvestmentCalculator createRoiCalculator( final LocalDate earliestDate,
			final EventListener roiDisplay, final MathContext MATH_CONTEXT ) {

		final CulmativeReturnOnInvestmentCalculator roi = new CulmativeReturnOnInvestmentCalculator( MATH_CONTEXT );
		final PeriodicCulmativeReturnOnInvestmentCalculatorListener dailyRoi = new PeriodicCulmativeReturnOnInvestmentCalculatorListener(
				earliestDate, Period.ofDays( 1 ), MATH_CONTEXT );
		final PeriodicCulmativeReturnOnInvestmentCalculatorListener monthlyRoi = new PeriodicCulmativeReturnOnInvestmentCalculatorListener(
				earliestDate, Period.ofMonths( 1 ), MATH_CONTEXT );
		final PeriodicCulmativeReturnOnInvestmentCalculatorListener yearlyRoi = new PeriodicCulmativeReturnOnInvestmentCalculatorListener(
				earliestDate, Period.ofYears( 1 ), MATH_CONTEXT );
		yearlyRoi.addListener( roiDisplay );
		monthlyRoi.addListener( roiDisplay );
		dailyRoi.addListener( roiDisplay );
		roi.addListener( dailyRoi );
		roi.addListener( monthlyRoi );
		roi.addListener( yearlyRoi );

		return roi;
	}

	public static LocalDate getEarliestDate( final TradingDayPrices[] data ) {
		LocalDate earliest = data[0].getDate();

		for (final TradingDayPrices today : data) {
			if (earliest.isAfter( today.getDate() )) {
				earliest = today.getDate();
			}
		}

		return earliest;
	}
}