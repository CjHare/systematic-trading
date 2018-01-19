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
package com.systematic.trading.backtest.output.file.dao.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Map;

import com.systematic.trading.backtest.output.file.dao.EventStatisticsDao;
import com.systematic.trading.backtest.output.file.util.FileMultithreading;
import com.systematic.trading.backtest.output.file.util.HistogramOutput;
import com.systematic.trading.simulation.analysis.statistics.EventStatistics;
import com.systematic.trading.simulation.analysis.statistics.event.BrokerageEventStatistics;
import com.systematic.trading.simulation.analysis.statistics.event.CashEventStatistics;
import com.systematic.trading.simulation.analysis.statistics.event.EquityEventStatistics;
import com.systematic.trading.simulation.analysis.statistics.event.OrderEventStatistics;

/**
 * Displays the summary of the events that occurred during processing.
 * 
 * @author CJ Hare
 */
public class FileEventStatisticsDao implements EventStatisticsDao {

	private static final DecimalFormat TWO_DECIMAL_PLACES = new DecimalFormat(".##");

	/** Display responsible for handling the file output. */
	private final FileMultithreading file;

	private final HistogramOutput histogram;

	private final EventStatistics statistics;

	public FileEventStatisticsDao( final EventStatistics statistics, final FileMultithreading file ) {

		this.statistics = statistics;
		this.file = file;
		this.histogram = new HistogramOutput();
	}

	@Override
	public void eventStatistics() {

		final StringBuilder output = new StringBuilder();

		output.append(String.format("##########################%n"));
		output.append(String.format("### Summary Statistics ###%n"));
		output.append(String.format("##########################%n"));

		addOrderStatistics(statistics.orderEventStatistics(), output);
		addCashStatistics(statistics.cashEventStatistics(), output);
		addBrokerageStatistics(statistics.brokerageEventStatistics(), output);
		addEquityStatistics(statistics.equityEventStatistics(), output);

		file.write(output.toString());
	}

	private void addOrderStatistics( final OrderEventStatistics orderStatistics, final StringBuilder output ) {

		output.append(String.format("%n=== Order events ===%n"));
		output.append(String.format("# Entry Order events: %s%n", orderStatistics.entryEventCount()));
		output.append(String.format("# Delete Entry Order events: %s%n", orderStatistics.deleteEntryEventCount()));
		output.append(String.format("# Exit Order events: %s%n", orderStatistics.exitEventCount()));
		output.append(String.format("# Delete Exit Order events: %s%n", orderStatistics.deleteExitEventCount()));
	}

	private void addCashStatistics( final CashEventStatistics cashStatistics, final StringBuilder output ) {

		output.append(String.format("%n=== Cash events ===%n"));
		output.append(String.format("# Cash account credit events: %s%n", cashStatistics.creditEventCount()));
		output.append(String.format("# Cash account debit events: %s%n", cashStatistics.debitEventCount()));
		output.append(String.format("# Cash account interest events: %s%n", cashStatistics.interestEventCount()));
		output.append(String.format("# Cash account deposit events: %s%n", cashStatistics.depositEventCount()));
		output.append(String.format("Total interest earned: %s%n",
		        TWO_DECIMAL_PLACES.format(cashStatistics.interestEarned())));
		output.append(String.format("Total amount deposited: %s%n",
		        TWO_DECIMAL_PLACES.format(cashStatistics.amountDeposited())));
	}

	private void addBrokerageStatistics( final BrokerageEventStatistics brokerageStatistics,
	        final StringBuilder output ) {

		final BigInteger sumBrokerageEvents = brokerageStatistics.sellEventCount()
		        .add(brokerageStatistics.buyEventCount());

		output.append(String.format("%n=== Brokerage events ===%n"));
		output.append(String.format("# Brokerage events: %s%n", sumBrokerageEvents));
		output.append(String.format("# Sell events: %s%n", brokerageStatistics.sellEventCount()));
		output.append(String.format("# Buy events: %s%n", brokerageStatistics.buyEventCount()));
		output.append(String.format("Total amount paid in brokerage: %s%n",
		        TWO_DECIMAL_PLACES.format(brokerageStatistics.brokerageFees())));

		addBrokerageBuyHistogram(brokerageStatistics, output);
	}

	private void addBrokerageBuyHistogram( final BrokerageEventStatistics brokerageStatistics,
	        final StringBuilder output ) {

		output.append(String.format("%n=== Brokerage Buy Histogram ===%n"));
		final Map<BigDecimal, BigInteger> buyEvents = brokerageStatistics.buyEvents();
		histogram.addHistogram(buyEvents, output);
	}

	private void addEquityStatistics( final EquityEventStatistics equityStatistics, final StringBuilder output ) {

		output.append(String.format("%n=== Equity events ===%n"));
		output.append(String.format("Total amount of equities paid in management fees: %s%n",
		        TWO_DECIMAL_PLACES.format(equityStatistics.totalManagmentFeesInEquities())));
	}
}