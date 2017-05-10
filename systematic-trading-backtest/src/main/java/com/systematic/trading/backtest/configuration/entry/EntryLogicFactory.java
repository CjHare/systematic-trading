/**
 * Copyright (c) 2015-2017, CJ Hare All rights reserved.
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
package com.systematic.trading.backtest.configuration.entry;

import java.math.MathContext;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.systematic.trading.backtest.BacktestSimulationDates;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.model.EquityIdentity;
import com.systematic.trading.signals.AnalysisBuySignals;
import com.systematic.trading.signals.AnalysisLongBuySignals;
import com.systematic.trading.signals.indicator.IndicatorSignalGenerator;
import com.systematic.trading.signals.model.filter.RollingTimePeriodSignalFilterDecorator;
import com.systematic.trading.signals.model.filter.SignalFilter;
import com.systematic.trading.signals.model.filter.TimePeriodSignalFilterDecorator;
import com.systematic.trading.simulation.logic.DateTriggeredEntryLogic;
import com.systematic.trading.simulation.logic.EntryLogic;
import com.systematic.trading.simulation.logic.SignalTriggeredEntryLogic;
import com.systematic.trading.simulation.logic.trade.TradeValueLogic;

/**
 * Creates the instances of Entry Logic.
 * 
 * @author CJ Hare
 */
public class EntryLogicFactory {

	/* Number of days of signals to use when triggering signals.*/
	private static final int DAYS_ACCEPTING_SIGNALS = 5;

	private static final EntryLogicFactory INSTANCE = new EntryLogicFactory();

	private EntryLogicFactory() {
	}

	public static final EntryLogicFactory getInstance() {
		return INSTANCE;
	}

	public EntryLogic create( final EquityIdentity equity, final LocalDate startDate,
	        final DepositConfiguration deposit, final MathContext mathContext ) {
		final Period frequency = deposit.getFrequency();
		return new DateTriggeredEntryLogic(equity.getType(), equity.getScale(), startDate, frequency, mathContext);
	}

	public EntryLogic create( final EquityIdentity equity, final LocalDate startDate, final Period frequency,
	        final MathContext mathContext ) {
		return new DateTriggeredEntryLogic(equity.getType(), equity.getScale(), startDate, frequency, mathContext);
	}

	public EntryLogic create( final EquityIdentity equity, final TradeValueLogic tradeValue,
	        final BacktestSimulationDates simulationDates, final SignalFilter filter, final MathContext mathContext,
	        final IndicatorSignalGenerator[] entrySignals ) {
		final LocalDate simulationStartDate = simulationDates.getStartDate();
		final LocalDate simulationEndDate = simulationDates.getEndDate();

		final List<SignalFilter> filters = new ArrayList<>();
		final SignalFilter decoratedFilter = new TimePeriodSignalFilterDecorator(
		        new RollingTimePeriodSignalFilterDecorator(filter, Period.ofDays(DAYS_ACCEPTING_SIGNALS)),
		        simulationStartDate, simulationEndDate);
		filters.add(decoratedFilter);

		final AnalysisBuySignals buyLongAnalysis = new AnalysisLongBuySignals(Arrays.asList(entrySignals), filters);
		return new SignalTriggeredEntryLogic(equity.getType(), equity.getScale(), tradeValue, buyLongAnalysis,
		        mathContext);
	}
}