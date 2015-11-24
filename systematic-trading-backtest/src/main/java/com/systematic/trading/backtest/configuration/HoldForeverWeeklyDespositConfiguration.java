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

import com.systematic.trading.backtest.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.brokerage.Brokerage;
import com.systematic.trading.backtest.brokerage.EquityIdentity;
import com.systematic.trading.backtest.brokerage.SingleEquityClassBroker;
import com.systematic.trading.backtest.brokerage.fees.BrokerageFeeStructure;
import com.systematic.trading.backtest.brokerage.fees.CmcMarketsFeeStructure;
import com.systematic.trading.backtest.cash.CalculatedDailyPaidMonthlyCashAccount;
import com.systematic.trading.backtest.cash.CashAccount;
import com.systematic.trading.backtest.cash.FlatInterestRate;
import com.systematic.trading.backtest.cash.InterestRate;
import com.systematic.trading.backtest.cash.RegularDepositCashAccountDecorator;
import com.systematic.trading.backtest.logic.EntryLogic;
import com.systematic.trading.backtest.logic.ExitLogic;
import com.systematic.trading.backtest.logic.HoldForeverExitLogic;
import com.systematic.trading.backtest.logic.MinimumTradeValue;
import com.systematic.trading.backtest.logic.SignalTriggeredEntryLogic;
import com.systematic.trading.signals.AnalysisBuySignals;
import com.systematic.trading.signals.indicator.IndicatorSignalGenerator;
import com.systematic.trading.signals.indicator.IndicatorSignalType;
import com.systematic.trading.signals.model.AnalysisLongBuySignals;
import com.systematic.trading.signals.model.filter.IndicatorsOnSameDaySignalFilter;
import com.systematic.trading.signals.model.filter.SignalFilter;
import com.systematic.trading.signals.model.filter.TimePeriodSignalFilterDecorator;

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
public class HoldForeverWeeklyDespositConfiguration extends DefaultConfiguration implements
		BacktestBootstrapConfiguration {

	/** Scale and precision to apply to mathematical operations. */
	private final MathContext mathContext;

	/** Input for the trading logic, determines the minimum value for transactions. */
	private final MinimumTradeValue minimumTrade;

	/** Signal generator for the entry logic. */
	private final IndicatorSignalGenerator[] entrySignals;

	/** Description used for uniquely identifying the configuration. */
	private final String description;

	public HoldForeverWeeklyDespositConfiguration( final LocalDate startDate, final LocalDate endDate,
			final MinimumTradeValue minimumTrade, final String description, final MathContext mathContext,
			final IndicatorSignalGenerator... entrySignals ) {
		super( startDate, endDate );

		if (entrySignals == null) {
			throw new IllegalArgumentException( "Indicator signal generators are needed for entry logic" );
		}

		this.entrySignals = entrySignals;
		this.minimumTrade = minimumTrade;
		this.description = description;
		this.mathContext = mathContext;
	}

	@Override
	public ExitLogic getExitLogic() {
		return new HoldForeverExitLogic();
	}

	@Override
	public Brokerage getBroker( final EquityIdentity equity ) {
		final BrokerageFeeStructure tradingFeeStructure = new CmcMarketsFeeStructure( mathContext );
		return new SingleEquityClassBroker( tradingFeeStructure, equity.getType(), mathContext );
	}

	@Override
	public CashAccount getCashAccount( final LocalDate openingDate ) {
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

		final List<IndicatorSignalGenerator> generators = new ArrayList<IndicatorSignalGenerator>( entrySignals.length );
		final IndicatorSignalType[] types = new IndicatorSignalType[entrySignals.length];

		for (int i = 0; i < entrySignals.length; i++) {
			final IndicatorSignalGenerator entrySignal = entrySignals[i];
			generators.add( entrySignal );
			types[i] = entrySignal.getSignalType();
		}

		// Only signals from the last two days are of interest
		final List<SignalFilter> filters = new ArrayList<SignalFilter>();
		final SignalFilter filter = new TimePeriodSignalFilterDecorator( new IndicatorsOnSameDaySignalFilter( types ),
				Period.ofDays( 5 ) );
		filters.add( filter );

		final AnalysisBuySignals buyLongAnalysis = new AnalysisLongBuySignals( generators, filters );
		return new SignalTriggeredEntryLogic( equity.getType(), minimumTrade, buyLongAnalysis, mathContext );
	}

	@Override
	public String getDescription() {
		return description;
	}
}
