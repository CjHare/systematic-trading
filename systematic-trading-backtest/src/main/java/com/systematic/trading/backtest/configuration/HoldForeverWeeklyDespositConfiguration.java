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
package com.systematic.trading.backtest.configuration;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.model.EquityIdentity;
import com.systematic.trading.signals.AnalysisBuySignals;
import com.systematic.trading.signals.AnalysisLongBuySignals;
import com.systematic.trading.signals.indicator.IndicatorSignalGenerator;
import com.systematic.trading.signals.model.IndicatorSignalType;
import com.systematic.trading.signals.model.filter.IndicatorsOnSameDaySignalFilter;
import com.systematic.trading.signals.model.filter.SignalFilter;
import com.systematic.trading.signals.model.filter.TimePeriodSignalFilterDecorator;
import com.systematic.trading.simulation.brokerage.Brokerage;
import com.systematic.trading.simulation.brokerage.SingleEquityClassBroker;
import com.systematic.trading.simulation.brokerage.fees.BrokerageFeeStructure;
import com.systematic.trading.simulation.cash.CalculatedDailyPaidMonthlyCashAccount;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.cash.FlatInterestRate;
import com.systematic.trading.simulation.cash.InterestRate;
import com.systematic.trading.simulation.cash.RegularDepositCashAccountDecorator;
import com.systematic.trading.simulation.logic.EntryLogic;
import com.systematic.trading.simulation.logic.ExitLogic;
import com.systematic.trading.simulation.logic.HoldForeverExitLogic;
import com.systematic.trading.simulation.logic.SignalTriggeredEntryLogic;
import com.systematic.trading.simulation.logic.TradeValue;

/**
 * Configuration for signal triggered entry logic, with weekly contribution to cash account.
 * <p/>
 * <ul>
 * <li>Exit logic: never sell</li>
 * <li>Cash account: zero starting, weekly 100 dollar deposit</li>
 * </ul>
 * 
 * @author CJ Hare
 */
public class HoldForeverWeeklyDespositConfiguration extends DefaultConfiguration
		implements BacktestBootstrapConfiguration {

	/** Number of days of signals to use when triggering signals. */
	private static final int DAYS_ACCEPTING_SIGNALS = 5;

	/** Scale and precision to apply to mathematical operations. */
	private final MathContext mathContext;

	/** Minimum value of the trade excluding the fee amount */
	private final TradeValue tradeValue;

	/** Signal generator for the entry logic. */
	private final IndicatorSignalGenerator[] entrySignals;

	/** Description used for uniquely identifying the configuration. */
	private final String description;

	/** Fees applied to each equity transaction. */
	private final BrokerageFeeStructure tradingFeeStructure;

	public HoldForeverWeeklyDespositConfiguration( final LocalDate startDate, final LocalDate endDate,
			final TradeValue tradeValue, final String description, final BrokerageFeeStructure tradingFeeStructure,
			final MathContext mathContext, final IndicatorSignalGenerator... entrySignals ) {
		
		//TODO use a single equity configuration, no abstract parent
		super( startDate, endDate );

		if (entrySignals == null) {
			throw new IllegalArgumentException( "Indicator signal generators are needed for entry logic" );
		}

		this.tradingFeeStructure = tradingFeeStructure;
		this.entrySignals = entrySignals;
		this.description = description;
		this.mathContext = mathContext;
		this.tradeValue = tradeValue;
	}

	@Override
	public ExitLogic getExitLogic() {
		return new HoldForeverExitLogic();
	}
	
	@Override
	public Brokerage getBroker( final EquityIdentity equity ) {
		return new SingleEquityClassBroker( tradingFeeStructure, equity.getType(), mathContext );
	}

	@Override
	public CashAccount getCashAccount( final LocalDate openingDate ) {
		
		//TODO all this into a new factory
		final Period weekly = Period.ofDays( 7 );
		final BigDecimal oneHundredDollars = BigDecimal.valueOf( 100 );
		final InterestRate annualInterestRate = new FlatInterestRate( BigDecimal.valueOf( 1.5 ), mathContext );
		final BigDecimal openingFunds = BigDecimal.valueOf( 100 );
		final CashAccount underlyingAccount = new CalculatedDailyPaidMonthlyCashAccount( annualInterestRate,
				openingFunds, openingDate, mathContext );
		return new RegularDepositCashAccountDecorator( oneHundredDollars, underlyingAccount, openingDate, weekly );
	}

	@Override
	public EntryLogic getEntryLogic( final EquityIdentity equity, final LocalDate openingDate ) {

		//TODO pass all this in, decide configuration outside, another factory.
		final List<IndicatorSignalGenerator> generators = new ArrayList<IndicatorSignalGenerator>(
				entrySignals.length );
		final IndicatorSignalType[] types = new IndicatorSignalType[entrySignals.length];

		for (int i = 0; i < entrySignals.length; i++) {
			final IndicatorSignalGenerator entrySignal = entrySignals[i];
			generators.add( entrySignal );
			types[i] = entrySignal.getSignalType();
		}

		// Only signals from the last few days are of interest
		final List<SignalFilter> filters = new ArrayList<SignalFilter>();
		final SignalFilter filter = new TimePeriodSignalFilterDecorator( new IndicatorsOnSameDaySignalFilter( types ),
				Period.ofDays( DAYS_ACCEPTING_SIGNALS ) );
		filters.add( filter );

		final AnalysisBuySignals buyLongAnalysis = new AnalysisLongBuySignals( generators, filters );
		return new SignalTriggeredEntryLogic( equity.getType(), tradeValue, buyLongAnalysis, mathContext );
	}

	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * The most number of days of signals used when analysing signals.
	 * 
	 * @return most number of days of signals analysed.
	 */
	public static int maximumDaysOfSignalsAnalysed() {
		return DAYS_ACCEPTING_SIGNALS;
	}
}
