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

import java.time.Period;
import java.util.List;
import java.util.StringJoiner;

import com.systematic.trading.backtest.context.BacktestBootstrapContext;
import com.systematic.trading.signals.model.filter.SignalFilter;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.cash.RegularDepositCashAccountDecorator;
import com.systematic.trading.simulation.logic.DateTriggeredEntryLogic;
import com.systematic.trading.simulation.logic.EntryLogic;
import com.systematic.trading.simulation.logic.trade.TradeValueCalculator;

/**
 * Textually meaningful description of the configuration appropriate for display.
 * 
 * @author CJ Hare
 */
public class DescriptionGenerator {
	// TODO interface - one for file, another for console
	//TODO convert to a builder

	private static final String SEPARATOR = "_";

	public String getDescription( final BacktestBootstrapContext configuration ) {
		final StringJoiner out = new StringJoiner(SEPARATOR);

		//TODO tell between different filter values, i.e. short / medium / long parameters ...or include those in filter output
		out.add(entryFilters(configuration.getEntryLogic()));
		out.add(minimumTradeValue(configuration.getEntryLogic()));
		out.add(maximumTradeValue(configuration.getEntryLogic()));

		//TODO weekly cash deposit / starting value
		out.add(depositAmount(configuration.getCashAccount()));

		//TODO add broker type
		out.add(configuration.getBroker().getName());

		//TODO better way of passing this information then the full objects, some configuration summary?

		//TODO add entry filters		

		//TODO add exit filters

		return String.format("%s_HoldForever", out.toString());
	}

	private String depositAmount( final CashAccount cashAccount ) {
		if (cashAccount instanceof RegularDepositCashAccountDecorator) {
			final RegularDepositCashAccountDecorator deposit = (RegularDepositCashAccountDecorator) cashAccount;
			return String.format("%s%s%s", deposit.getDepositAmount(), SEPARATOR, deposit.getInterval());
		}

		return "NoDeposit";
	}

	private String entryFilters( final EntryLogic entryLogic ) {
		if (entryLogic.getBuySignalAnalysis() == null) {
			if (Period.ofDays(7).equals(((DateTriggeredEntryLogic) entryLogic).getInterval())) {
				return "BuyWeekly";
			} else {
				return "BuyMonthly";
			}
		}

		final List<SignalFilter> filters = entryLogic.getBuySignalAnalysis().getFilters();
		final StringJoiner out = new StringJoiner(SEPARATOR);
		for (final SignalFilter filter : filters) {
			//out.add(filter.getDescription(SEPARATOR));
		}
		return out.toString();
	}

	private String maximumTradeValue( final EntryLogic entryLogic ) {
		return tradeValue("Maximum", entryLogic.getTradeValue().getMinimumValue());
	}

	private String minimumTradeValue( final EntryLogic entryLogic ) {
		return tradeValue("Minimum", entryLogic.getTradeValue().getMinimumValue());
	}

	private String tradeValue( final String prefix, final TradeValueCalculator tradeValue ) {
		return String.format("%s-%s%s%s", prefix, tradeValue.getValue(), SEPARATOR, tradeValue.getType());
	}
}