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
package com.systematic.trading.backtest.display;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.StringJoiner;

import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.brokerage.BrokerageFeesConfiguration;
import com.systematic.trading.backtest.configuration.cash.CashAccountConfiguration;
import com.systematic.trading.backtest.configuration.entry.EntryLogicConfiguration;
import com.systematic.trading.backtest.configuration.entry.ExitLogicConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
import com.systematic.trading.backtest.configuration.signals.SignalConfiguration;
import com.systematic.trading.backtest.configuration.trade.MaximumTrade;
import com.systematic.trading.backtest.configuration.trade.MinimumTrade;

/**
 * Textually meaningful description of the configuration appropriate for display.
 * 
 * @author CJ Hare
 */
public class DescriptionGenerator {
	// TODO interface - one for file, another for console

	private static final String SEPARATOR = "_";

	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

	private static final DecimalFormat MAX_TWO_DECIMAL_PLACES = new DecimalFormat("#");

	public String getDescription( final BacktestBootstrapConfiguration configuration ) {
		final StringJoiner out = new StringJoiner(SEPARATOR);
		out.add(equity(configuration.getEquity()));
		out.add(brokerage(configuration.getBrokerageFees()));
		out.add(cashAccount(configuration.getCashAccount()));
		out.add(entryLogic(configuration.getEntryLogic()));
		out.add(exitLogic(configuration.getExitLogic()));
		out.add(minimumTradeValue(configuration.getMinimumTrade()));
		out.add(maximumTradeValue(configuration.getMaximumTrade()));
		return out.toString();
	}

	private String entryLogic( final EntryLogicConfiguration entry ) {
		switch (entry.getType()) {
			case CONFIRMATION_SIGNAL:
				return entryLogicConfirmationSignal(entry);

			case PERIODIC:
				return entryPeriodic(entry);

			case SAME_DAY_SIGNALS:
				return entryLogicSameDaySignals(entry);

			default:
				throw new IllegalArgumentException(String.format("Unacceptable entry logic type: %s", entry.getType()));
		}
	}

	private String entryPeriodic( final EntryLogicConfiguration entry ) {
		switch (entry.getPeriodic()) {
			case WEEKLY:
				return "Weekly";

			case MONTHLY:
				return "Monthly";

			default:
				throw new IllegalArgumentException(String.format("Unexpected perodic: %s", entry.getPeriodic()));
		}
	}

	//TODO need to know which signal is the anchor and which the confirmation
	private String entryLogicConfirmationSignal( final EntryLogicConfiguration entry ) {
		final StringJoiner out = new StringJoiner(SEPARATOR);
		out.add(entry.getConfirmationSignal().name());
		for (final SignalConfiguration signal : entry.getSignals()) {
			out.add(signal.getDescription());
		}
		return out.toString();
	}

	private String entryLogicSameDaySignals( final EntryLogicConfiguration entry ) {
		final StringJoiner out = new StringJoiner(SEPARATOR);
		out.add("SameDay");
		for (final SignalConfiguration signal : entry.getSignals()) {
			out.add(signal.getDescription());
		}
		return out.toString();
	}

	private String equity( final EquityConfiguration equity ) {
		return equity.getEquityIdentity().getTickerSymbol();
	}

	private String exitLogic( final ExitLogicConfiguration entry ) {
		return "HoldForever";
	}

	private String cashAccount( final CashAccountConfiguration cashAccount ) {
		switch (cashAccount) {
			case CALCULATED_DAILY_PAID_MONTHLY:
				return "InterestDaily"; // Standard output needs no description

			default:
				return "NoInterest";
		}
	}

	private String brokerage( final BrokerageFeesConfiguration brokerage ) {
		switch (brokerage) {
			case CMC_MARKETS:
				return "CmC";

			case VANGUARD_RETAIL:
				return "Vanguard";

			default:
				throw new IllegalArgumentException(String.format("Unexpected brokerage fee %s", brokerage));
		}
	}

	private String minimumTradeValue( final MinimumTrade trade ) {
		return String.format("Minimum%s", MAX_TWO_DECIMAL_PLACES.format(trade.getValue()));
	}

	private String maximumTradeValue( final MaximumTrade trade ) {
		return String.format("Maximum%s%spercent", convertToPercetage(trade.getValue()), SEPARATOR);
	}

	private String convertToPercetage( final BigDecimal toPercentage ) {
		return String.format("%s", MAX_TWO_DECIMAL_PLACES.format(toPercentage.multiply(ONE_HUNDRED)));
	}
}