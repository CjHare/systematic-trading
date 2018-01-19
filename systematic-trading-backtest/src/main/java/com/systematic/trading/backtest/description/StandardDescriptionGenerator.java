/**
 * Copyright (c) 2015-2018, CJ Hare All rights reserved.
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
package com.systematic.trading.backtest.description;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Period;
import java.util.StringJoiner;

import com.systematic.trading.backtest.brokerage.fee.CmcMarketsBrokerageFees;
import com.systematic.trading.backtest.brokerage.fee.SelfWealthBrokerageFees;
import com.systematic.trading.backtest.brokerage.fee.VanguardBrokerageFees;
import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.cash.DepositConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.configuration.strategy.confirmation.ConfirmaByConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.EntryConfiguration;
import com.systematic.trading.backtest.configuration.strategy.entry.size.EntrySizeConfiguration;
import com.systematic.trading.backtest.configuration.strategy.exit.ExitConfiguration;
import com.systematic.trading.backtest.configuration.strategy.exit.size.ExitSizeConfiguration;
import com.systematic.trading.backtest.configuration.strategy.operator.OperatorConfiguration;
import com.systematic.trading.backtest.configuration.strategy.periodic.PeriodicConfiguration;
import com.systematic.trading.backtest.trade.MaximumTrade;
import com.systematic.trading.backtest.trade.MinimumTrade;
import com.systematic.trading.simulation.brokerage.fee.BrokerageTransactionFeeStructure;
import com.systematic.trading.strategy.indicator.configuration.IndicatorConfiguration;

/**
 * Description generator with no restriction on the length of generated description.
 * 
 * @author CJ Hare
 */
public class StandardDescriptionGenerator implements DescriptionGenerator {

	// TODO create two level description generator - don't want to use this as parameter!

	private static final String SEPARATOR = "_";
	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
	private static final DecimalFormat NO_DECIMAL_PLACES = new DecimalFormat("#");
	private static final String OPERATOR_PREFIX = "(";
	private static final String OPERATOR_SUFFIX = ")";

	@Override
	public String strategy( final EntryConfiguration entry, final EntrySizeConfiguration entryPositionSizing,
	        final ExitConfiguration exit, final ExitSizeConfiguration exitPositionSizing ) {

		final StringJoiner out = new StringJoiner(SEPARATOR);
		out.add(entry.description(this));
		out.add(entryPositionSizing.description(this));
		out.add(exit.description(this));
		out.add(exitPositionSizing.description(this));
		return out.toString();
	}

	@Override
	public String positionSize( final MinimumTrade minimumTrade, final MaximumTrade maximumTrade ) {

		final StringJoiner out = new StringJoiner(SEPARATOR);
		out.add(minimumTradeValue(minimumTrade));
		out.add(maximumTradeValue(maximumTrade));
		return out.toString();
	}

	@Override
	public String bootstrapConfiguration( final BacktestBootstrapConfiguration configuration ) {

		final StringJoiner out = new StringJoiner(SEPARATOR);
		out.add(equity(configuration.equity()));
		out.add(brokerage(configuration.brokerageFees()));
		out.add(configuration.strategy().description(this));
		return out.toString();
	}

	@Override
	public String bootstrapConfigurationWithDeposit( final BacktestBootstrapConfiguration configuration,
	        final DepositConfiguration depositAmount ) {

		final StringJoiner out = new StringJoiner(SEPARATOR);
		out.add(equity(configuration.equity()));
		out.add(brokerage(configuration.brokerageFees()));
		out.add(deposit(depositAmount));
		out.add(configuration.strategy().description(this));
		return out.toString();
	}

	@Override
	public String periodicEntry( final PeriodicConfiguration frequency ) {

		switch (frequency) {
			case WEEKLY:
				return "Buy-Weekly";

			case MONTHLY:
				return "Buy-Monthly";

			default:
				throw new IllegalArgumentException(String.format("Unexpected perodic: %s", frequency));
		}
	}

	@Override
	public String indicator( final IndicatorConfiguration indicator ) {

		return indicator.id().name();
	}

	@Override
	public String entry( final EntryConfiguration leftEntry, final OperatorConfiguration.Selection op,
	        final EntryConfiguration righEntry ) {

		final StringBuilder out = new StringBuilder();
		out.append(entryDisplay(leftEntry));
		out.append(SEPARATOR);
		out.append(op.name());
		out.append(SEPARATOR);
		out.append(entryDisplay(righEntry));
		return out.toString();
	}

	@Override
	public String entry( final EntryConfiguration anchor, final ConfirmaByConfiguration confirmBy,
	        final EntryConfiguration confirmation ) {

		final int delay = confirmBy.delayUntilConfirmationRange();
		final int range = confirmBy.confirmationDayRange();
		final StringJoiner out = new StringJoiner(SEPARATOR);
		out.add(entryDisplay(anchor));
		out.add("confirmedBy");
		out.add(entryDisplay(confirmation));
		out.add("in");
		out.add(String.valueOf(delay));
		out.add("to");
		out.add(String.valueOf(delay + range));
		out.add("days");
		return out.toString();
	}

	private String deposit( final DepositConfiguration depositAmount ) {

		final StringJoiner out = new StringJoiner(SEPARATOR);
		out.add("Deposit");
		out.add(String.valueOf(depositAmount.amount()));
		out.add(textualDisplay(depositAmount.frequency().period()));
		return out.toString();
	}

	private String textualDisplay( final Period time ) {

		if (Period.ofDays(7).equals(time)) { return "Weekly"; }
		if (Period.ofMonths(1).equals(time)) { return "Monthly"; }
		if (Period.ofYears(1).equals(time)) { return "Yearly"; }

		return time.toString();
	}

	private String entryDisplay( final EntryConfiguration entry ) {

		final StringBuilder out = new StringBuilder();
		if (entry.hasSubEntry()) {
			out.append(OPERATOR_PREFIX);
		}
		out.append(entry.description(this));
		if (entry.hasSubEntry()) {
			out.append(OPERATOR_SUFFIX);
		}

		return out.toString();
	}

	private String equity( final EquityConfiguration equity ) {

		return equity.gquityIdentity().tickerSymbol();
	}

	private String brokerage( final BrokerageTransactionFeeStructure brokerage ) {

		if (brokerage instanceof CmcMarketsBrokerageFees) { return "CmcMarkets"; }
		if (brokerage instanceof VanguardBrokerageFees) { return "Vanguard"; }
		if (brokerage instanceof SelfWealthBrokerageFees) { return "SelfWealth"; }

		throw new IllegalArgumentException(String.format("Unexpected brokerage: %s", brokerage));
	}

	private String minimumTradeValue( final MinimumTrade trade ) {

		return String.format("Minimum%s%s", SEPARATOR, NO_DECIMAL_PLACES.format(trade.value()));
	}

	private String maximumTradeValue( final MaximumTrade trade ) {

		final StringJoiner out = new StringJoiner(SEPARATOR);
		out.add("Maximum");
		out.add(convertToPercetage(trade.value()));
		out.add("percent");
		return out.toString();
	}

	private String convertToPercetage( final BigDecimal toPercentage ) {

		return String.format("%s", NO_DECIMAL_PLACES.format(toPercentage.multiply(ONE_HUNDRED)));
	}
}