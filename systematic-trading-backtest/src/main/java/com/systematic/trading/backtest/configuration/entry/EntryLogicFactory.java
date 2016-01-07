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
package com.systematic.trading.backtest.configuration.entry;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.systematic.trading.model.EquityIdentity;
import com.systematic.trading.signals.AnalysisBuySignals;
import com.systematic.trading.signals.AnalysisLongBuySignals;
import com.systematic.trading.signals.indicator.IndicatorSignalGenerator;
import com.systematic.trading.signals.model.IndicatorSignalType;
import com.systematic.trading.signals.model.filter.SignalFilter;
import com.systematic.trading.signals.model.filter.TimePeriodSignalFilterDecorator;
import com.systematic.trading.simulation.logic.DateTriggeredEntryLogic;
import com.systematic.trading.simulation.logic.EntryLogic;
import com.systematic.trading.simulation.logic.SignalTriggeredEntryLogic;
import com.systematic.trading.simulation.logic.TradeValue;

/**
 * Creates the instances of Entry Logic.
 * 
 * @author CJ Hare
 */
public class EntryLogicFactory {

	/** Classes logger. */
	private static final Logger LOG = LogManager.getLogger( EntryLogicFactory.class );

	public static EntryLogic create( final EquityIdentity equity, final LocalDate startDate,
			final MathContext mathContext ) {
		final Period weekly = Period.ofDays( 7 );
		final BigDecimal oneHundredDollars = BigDecimal.valueOf( 100 );
		return new DateTriggeredEntryLogic( oneHundredDollars, equity.getType(), startDate, weekly, mathContext );
	}

	public static EntryLogic create( final EquityIdentity equity, final TradeValue tradeValue,
			final EntryLogicFilterConfiguration filterConfiguration, final MathContext mathContext,
			final IndicatorSignalGenerator... entrySignals ) {

		final List<IndicatorSignalGenerator> generators = new ArrayList<IndicatorSignalGenerator>(
				entrySignals.length );
		final IndicatorSignalType[] types = new IndicatorSignalType[entrySignals.length];

		for (int i = 0; i < entrySignals.length; i++) {
			final IndicatorSignalGenerator entrySignal = entrySignals[i];
			generators.add( entrySignal );
			types[i] = entrySignal.getSignalType();
		}

		// Number of days of signals to use when triggering signals.
		final int DAYS_ACCEPTING_SIGNALS = 5;

		// Only signals from the last few days are of interest
		final List<SignalFilter> filters = new ArrayList<SignalFilter>();
		final SignalFilter filter = creatSignalFilter( filterConfiguration, entrySignals );
		final SignalFilter decoratedFilter = new TimePeriodSignalFilterDecorator( filter,
				Period.ofDays( DAYS_ACCEPTING_SIGNALS ) );
		filters.add( decoratedFilter );

		final AnalysisBuySignals buyLongAnalysis = new AnalysisLongBuySignals( generators, filters );
		return new SignalTriggeredEntryLogic( equity.getType(), tradeValue, buyLongAnalysis, mathContext );
	}

	private static SignalFilter creatSignalFilter( final EntryLogicFilterConfiguration configuration,
			final IndicatorSignalGenerator[] entrySignals ) {

		try {
			Constructor<?> cons = configuration.getType().getConstructor( IndicatorSignalGenerator[].class );
			final Object[] passed = { entrySignals };
			return (SignalFilter) cons.newInstance( passed );
		} catch (final NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			LOG.error( e );
		}

		throw new IllegalArgumentException(
				String.format( "Could not create the desired entry logic filter: %s", configuration ) );
	}
}
