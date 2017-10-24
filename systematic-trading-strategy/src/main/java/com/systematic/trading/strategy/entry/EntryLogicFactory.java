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
package com.systematic.trading.strategy.entry;

import java.time.LocalDate;
import java.time.Period;

import com.systematic.trading.model.EquityIdentity;
import com.systematic.trading.signals.AnalysisBuySignals;
import com.systematic.trading.signals.AnalysisLongBuySignals;
import com.systematic.trading.signals.generator.IndicatorSignals;
import com.systematic.trading.signals.model.filter.RollingTimePeriodSignalFilterDecorator;
import com.systematic.trading.signals.model.filter.SignalFilter;
import com.systematic.trading.signals.model.filter.TimePeriodSignalFilterDecorator;
import com.systematic.trading.simulation.logic.EntryLogic;
import com.systematic.trading.simulation.logic.trade.TradeValueLogic;

/**
 * Factory methods for creating instances of Entry Logic.
 * 
 * @author CJ Hare
 */
public class EntryLogicFactory {

	//TODO should be retrieved from the largest confirmed range, or one
	/* Number of days of signals to use when triggering signals.*/
	private static final int DAYS_ACCEPTING_SIGNALS = 5;

	public EntryLogic create( final EquityIdentity equity, final LocalDate startDate, final Period frequency ) {
		return new DateTriggeredEntryLogic(equity.getType(), equity.getScale(), startDate, frequency);
	}

	public EntryLogic create( final EquityIdentity equity, final TradeValueLogic tradeValue,
	        final LocalDate startDateInclusive, final LocalDate endDateInclusive, final SignalFilter filter,
	        final IndicatorSignals[] entrySignals ) {

		final SignalFilter[] filters = new SignalFilter[1];
		final SignalFilter decoratedFilter = new TimePeriodSignalFilterDecorator(
		        new RollingTimePeriodSignalFilterDecorator(filter, Period.ofDays(DAYS_ACCEPTING_SIGNALS)),
		        startDateInclusive, endDateInclusive);
		filters[0] = decoratedFilter;

		final AnalysisBuySignals buyLongAnalysis = new AnalysisLongBuySignals(entrySignals, filters);
		return new SignalTriggeredEntryLogic(equity.getType(), equity.getScale(), tradeValue, buyLongAnalysis);
	}
}