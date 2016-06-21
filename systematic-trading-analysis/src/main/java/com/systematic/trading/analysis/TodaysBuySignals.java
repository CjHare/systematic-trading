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
package com.systematic.trading.analysis;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.analysis.model.ProcessLongBuySignals;
import com.systematic.trading.analysis.view.DisplayBuySignals;
import com.systematic.trading.data.DataService;
import com.systematic.trading.data.DataServiceUpdater;
import com.systematic.trading.data.DataServiceUpdaterImpl;
import com.systematic.trading.data.HibernateDataService;
import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.util.HibernateUtil;
import com.systematic.trading.exception.ServiceException;
import com.systematic.trading.signals.indicator.IndicatorSignalGenerator;
import com.systematic.trading.signals.indicator.MovingAveragingConvergeDivergenceSignals;
import com.systematic.trading.signals.indicator.RelativeStrengthIndexSignals;
import com.systematic.trading.signals.indicator.SimpleMovingAverageGradientSignals;
import com.systematic.trading.signals.indicator.SimpleMovingAverageGradientSignals.GradientType;
import com.systematic.trading.signals.model.BuySignal;
import com.systematic.trading.signals.model.IndicatorSignalType;
import com.systematic.trading.signals.model.filter.IndicatorsOnSameDaySignalFilter;
import com.systematic.trading.signals.model.filter.RollingTimePeriodSignalFilterDecorator;
import com.systematic.trading.signals.model.filter.SignalFilter;

public class TodaysBuySignals {

	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;
	private static final Logger LOG = LogManager.getLogger(TodaysBuySignals.class);

	/** The oldest signal date (inclusive) from today to report, includes non-trading days. */
	private static final int OLDEST_SIGNAL = 6;

	/* Days data needed - 20 + 20 for the MACD part EMA(20), Weekend and bank holidays */
	private static final int HISTORY_REQUIRED = 80 + 16 + 5;

	public static void main( final String... args ) throws ServiceException {

		//TODO calculate the number of days from the indicators, their lookback, warm ups & days of interest
		updateEquities();

		final LocalDate endDate = LocalDate.now();
		final LocalDate startDate = endDate.minus(HISTORY_REQUIRED, ChronoUnit.DAYS);
		final MovingAveragingConvergeDivergenceSignals macd = new MovingAveragingConvergeDivergenceSignals(10, 20, 7,
		        MATH_CONTEXT);

		final int smaLookback = 50;
		final int daysOfSmaGradient = 7;
		final SimpleMovingAverageGradientSignals sma = new SimpleMovingAverageGradientSignals(smaLookback,
		        daysOfSmaGradient, GradientType.POSITIVE, MATH_CONTEXT);

		final int lookback = 10;
		final BigDecimal overbought = BigDecimal.valueOf(30);
		final BigDecimal oversold = BigDecimal.valueOf(70);
		final RelativeStrengthIndexSignals rsi = new RelativeStrengthIndexSignals(lookback, overbought, oversold,
		        MATH_CONTEXT);

		final List<IndicatorSignalGenerator> generators = new ArrayList<IndicatorSignalGenerator>();
		generators.add(macd);
		generators.add(sma);
		generators.add(rsi);

		final List<SignalFilter> filters = new ArrayList<SignalFilter>();
		final SignalFilter filter = new RollingTimePeriodSignalFilterDecorator(new IndicatorsOnSameDaySignalFilter(
		        IndicatorSignalType.MACD, IndicatorSignalType.SMA, IndicatorSignalType.RSI), Period.ofDays(5));
		filters.add(filter);

		final ProcessLongBuySignals buyLong = new ProcessLongBuySignals(generators, filters);

		final Map<Equity, List<BuySignal>> buyLongSignals = new EnumMap<Equity, List<BuySignal>>(Equity.class);

		for (final Equity equity : Equity.values()) {
			final TradingDayPrices[] dataPoints = getDataPoints(equity, startDate, endDate);
			final List<BuySignal> signals = buyLong.process(equity, dataPoints);
			buyLongSignals.put(equity, signals);
		}

		displayBuySignals(buyLongSignals);

		HibernateUtil.getSessionFactory().close();
	}

	private static void updateEquities() throws ServiceException {
		final DataServiceUpdater updateService = DataServiceUpdaterImpl.getInstance();
		final LocalDate endDate = LocalDate.now();
		final LocalDate startDate = endDate.minus(HISTORY_REQUIRED, ChronoUnit.DAYS);

		for (final Equity equity : Equity.values()) {
			updateService.get(equity.getSymbol(), startDate, endDate);
		}
	}

	private static void displayBuySignals( final Map<Equity, List<BuySignal>> buyLongSignals ) {
		final DisplayBuySignals display = new DisplayBuySignals(OLDEST_SIGNAL);

		for (final Equity equity : buyLongSignals.keySet()) {
			final String symbol = equity.getSymbol();
			display.displayBuySignals(symbol, buyLongSignals.get(equity));
		}
	}

	private static TradingDayPrices[] getDataPoints( final Equity equity, final LocalDate startDate,
	        final LocalDate endDate ) {
		final DataService service = HibernateDataService.getInstance();
		final String tickerSymbol = equity.getSymbol();
		final TradingDayPrices[] data = service.get(tickerSymbol, startDate, endDate);

		LOG.info(String.format("%s data points returned: %s", tickerSymbol, data == null ? null : data.length));

		return data;
	}
}
