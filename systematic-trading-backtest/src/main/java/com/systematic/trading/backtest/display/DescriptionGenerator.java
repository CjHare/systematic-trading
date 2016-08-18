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

import java.util.StringJoiner;

import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.brokerage.BrokerageFeesConfiguration;
import com.systematic.trading.backtest.configuration.cash.CashAccountConfiguration;
import com.systematic.trading.backtest.configuration.deposit.DepositConfiguration;
import com.systematic.trading.backtest.configuration.entry.EntryLogicConfiguration;
import com.systematic.trading.backtest.configuration.entry.ExitLogicConfiguration;
import com.systematic.trading.backtest.configuration.equity.EquityConfiguration;
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

	public String getDescription( final BacktestBootstrapConfiguration configuration ) {
		final StringJoiner out = new StringJoiner(SEPARATOR);

		//TODO tell between different filter values, i.e. short / medium / long parameters ...or include those in filter output

		out.add(equity(configuration.getEquity()));
		out.add(brokerage(configuration.getBrokerageFees()));
		out.add(cashAccount(configuration.getCashAccount()));
		out.add(depositAmount(configuration.getDeposit()));
		out.add(entryLogic(configuration.getEntryLogic()));
		out.add(exitLogic(configuration.getExitLogic()));
		out.add(minimumTradeValue(configuration.getMinimumTrade()));
		out.add(maximumTradeValue(configuration.getMaximumTrade()));

		return out.toString();
	}

	private String entryLogic( final EntryLogicConfiguration entry ) {

		switch (entry.getType()) {
			case CONFIRMATION_SIGNAL:
				return String.format("Buy%s%s", SEPARATOR, entry.getConfirmationSignal().name());

			case PERIODIC:
				return String.format("Buy%s%s", SEPARATOR, entry.getPeriodic().name());

			case SAME_DAY_SIGNALS:
				return String.format("Buy%s%s", SEPARATOR, entry.getSameDaySignals().name());

			default:
				throw new IllegalArgumentException(String.format("Unacceptable entry logic type: %s", entry.getType()));
		}
	}

	private String equity( final EquityConfiguration equity ) {
		return equity.getEquityIdentity().getTickerSymbol();
	}

	private String exitLogic( final ExitLogicConfiguration entry ) {
		return "HoldForever";
	}

	private String cashAccount( final CashAccountConfiguration cashAccount ) {
		return String.format("Account %s", cashAccount.name());
	}

	private String depositAmount( final DepositConfiguration deposit ) {
		return String.format("DepositAmoun%s%s%sDepositFrequency%s%s", SEPARATOR, deposit.getAmount(), SEPARATOR,
		        SEPARATOR, deposit.getFrequency());
	}

	private String brokerage( final BrokerageFeesConfiguration brokerage ) {
		return String.format("%s", brokerage.name());
	}

	private String minimumTradeValue( final MinimumTrade trade ) {
		return String.format("MinimumTrade%s%s", SEPARATOR, trade.getDescription());
	}

	private String maximumTradeValue( final MaximumTrade trade ) {
		return String.format("MaximumTrade%s%s%spercent", SEPARATOR, trade.getDescription(), SEPARATOR);
	}
}