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
import com.systematic.trading.backtest.brokerage.fees.BrokerageFeeStructure;
import com.systematic.trading.backtest.brokerage.fees.impl.CmcMarketsFeeStructure;
import com.systematic.trading.backtest.brokerage.impl.SingleEquityClassBroker;
import com.systematic.trading.backtest.cash.CashAccount;
import com.systematic.trading.backtest.cash.InterestRate;
import com.systematic.trading.backtest.cash.impl.CalculatedDailyPaidMonthlyCashAccount;
import com.systematic.trading.backtest.cash.impl.FlatInterestRate;
import com.systematic.trading.backtest.cash.impl.RegularDepositCashAccountDecorator;
import com.systematic.trading.backtest.logic.EntryLogic;
import com.systematic.trading.backtest.logic.ExitLogic;
import com.systematic.trading.backtest.logic.impl.HoldForeverExitLogic;
import com.systematic.trading.backtest.logic.impl.SignalTriggeredEntryLogic;
import com.systematic.trading.signals.AnalysisBuySignals;
import com.systematic.trading.signals.indicator.impl.MovingAveragingConvergeDivergenceSignals;
import com.systematic.trading.signals.indicator.impl.RelativeStrengthIndexSignals;
import com.systematic.trading.signals.indicator.impl.SimpleMovingAverageGradientSignals;
import com.systematic.trading.signals.indicator.impl.StochasticOscillatorSignals;
import com.systematic.trading.signals.indicator.impl.SimpleMovingAverageGradientSignals.Gradient;
import com.systematic.trading.signals.model.AnalysisLongBuySignals;
import com.systematic.trading.signals.model.configuration.AllSignalsConfiguration;
import com.systematic.trading.signals.model.configuration.LongBuySignalConfiguration;
import com.systematic.trading.signals.model.filter.MacdWithPositiveSmaSignalFilter;
import com.systematic.trading.signals.model.filter.SignalFilter;
import com.systematic.trading.signals.model.filter.TimePeriodSignalFilterDecorator;

/**
 * Configuration for signal triggered entry logic.
 * <p/>
 * <ul>
 * <li>Entry logic: MACD buy trigger when the 200 day Sma is a positive gradient</li>
 * <li>Exit logic: never sell</li>
 * <li>Cash account: zero starting, weekly 100 dollar deposit</li>
 * <li>Broker transaction: minimum 1000, use full cash balance</li>
 * </ul>
 * 
 * @author CJ Hare
 */
public class MacdPositiveSmaEntryHoldForeverWeeklyDespositConfiguration extends DefaultConfiguration implements
		BacktestBootstrapConfiguration {

	/** Scale and precision to apply to mathematical operations. */
	private final MathContext mathContext;

	public MacdPositiveSmaEntryHoldForeverWeeklyDespositConfiguration( final LocalDate startDate,
			final LocalDate endDate, final MathContext mathContext ) {
		super( startDate, endDate );
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

		final RelativeStrengthIndexSignals rsi = new RelativeStrengthIndexSignals( 70, 30 );
		final MovingAveragingConvergeDivergenceSignals macd = new MovingAveragingConvergeDivergenceSignals( 10, 20, 7 );
		final StochasticOscillatorSignals stochastic = new StochasticOscillatorSignals( 10, 3, 3 );
		final SimpleMovingAverageGradientSignals sma = new SimpleMovingAverageGradientSignals( 200, Gradient.POSITIVE, mathContext );
		final LongBuySignalConfiguration configuration = new AllSignalsConfiguration( rsi, macd, sma, stochastic );

		// Only signals from the last two days are of interest
		final List<SignalFilter> filters = new ArrayList<SignalFilter>();
		final SignalFilter filter = new TimePeriodSignalFilterDecorator( new MacdWithPositiveSmaSignalFilter(),
				Period.ofDays( 5 ) );
		filters.add( filter );

		final AnalysisBuySignals buyLongAnalysis = new AnalysisLongBuySignals( configuration, filters );
		final BigDecimal minimumTradeValue = BigDecimal.valueOf( 1000 );
		return new SignalTriggeredEntryLogic( equity.getType(), minimumTradeValue, buyLongAnalysis, mathContext );
	}

	@Override
	public String getDescription() {
		return "Macd-PositiveSma-Buy_HoldForever";
	}
}