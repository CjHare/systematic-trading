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
package com.systematic.trading.backtest.display.console;

import java.text.DecimalFormat;

import com.systematic.trading.backtest.display.EventStatisticsDisplay;
import com.systematic.trading.simulation.analysis.statistics.BrokerageEventStatistics;
import com.systematic.trading.simulation.analysis.statistics.CashEventStatistics;
import com.systematic.trading.simulation.analysis.statistics.EventStatistics;
import com.systematic.trading.simulation.analysis.statistics.OrderEventStatistics;

/**
 * Displays the summary of the events that occurred during processing.
 * 
 * @author CJ Hare
 */
public class ConsoleEventStatisticsDisplay implements EventStatisticsDisplay {

	private static final DecimalFormat TWO_DECIMAL_PLACES = new DecimalFormat(".##");

	private final EventStatistics statistics;

	public ConsoleEventStatisticsDisplay(final EventStatistics statistics) {
		this.statistics = statistics;
	}

	@Override
	public void displayEventStatistics() {
		System.out.println("\n");
		System.out.println("#####################");
		System.out.println("### Event Summary ###");
		System.out.println("#####################");

		displayOrderStatistics(statistics.getOrderEventStatistics());
		displayCashStatistics(statistics.getCashEventStatistics());
		displayBrokerageStatistics(statistics.getBrokerageEventStatistics());
	}

	private void displayOrderStatistics( final OrderEventStatistics orderStatistics ) {

		System.out.println("\n=== Order events ===");
		System.out.println(String.format("# Entry Order events: %s", orderStatistics.getEntryEventCount()));
		System.out
		        .println(String.format("# Delete Entry Order events: %s", orderStatistics.getDeleteEntryEventCount()));
		System.out.println(String.format("# Exit Order events: %s", orderStatistics.getExitEventCount()));
		System.out.println(String.format("# Delete Exit Order events: %s", orderStatistics.getDeleteExitEventCount()));
	}

	private void displayCashStatistics( final CashEventStatistics cashStatistics ) {

		System.out.println("\n=== Cash events ===");
		System.out.println(String.format("# Cash account credit events: %s", cashStatistics.getCreditEventCount()));
		System.out.println(String.format("# Cash account debit events: %s", cashStatistics.getDebitEventCount()));
		System.out.println(String.format("# Cash account interest events: %s", cashStatistics.getInterestEventCount()));
		System.out.println(String.format("# Cash account deposit events: %s", cashStatistics.getDepositEventCount()));
		System.out.println(String.format("Total interest earned: %s",
		        TWO_DECIMAL_PLACES.format(cashStatistics.getInterestEarned())));
		System.out.println(String.format("Total amount deposited: %s",
		        TWO_DECIMAL_PLACES.format(cashStatistics.getAmountDeposited())));
	}

	private void displayBrokerageStatistics( final BrokerageEventStatistics brokerageStatistics ) {

		final long sumBrokerageEvents = brokerageStatistics.getSellEventCount()
		        + brokerageStatistics.getBuyEventCount();

		System.out.println("\n=== Brokerage events ===");
		System.out.println(String.format("# Brokerage events: %s", sumBrokerageEvents));
		System.out.println(String.format("# Sell events: %s", brokerageStatistics.getSellEventCount()));
		System.out.println(String.format("# Buy events: %s", brokerageStatistics.getBuyEventCount()));
		System.out.println(String.format("Total amount paid in brokerage: %s",
		        TWO_DECIMAL_PLACES.format(brokerageStatistics.getBrokerageFees())));
	}
}
