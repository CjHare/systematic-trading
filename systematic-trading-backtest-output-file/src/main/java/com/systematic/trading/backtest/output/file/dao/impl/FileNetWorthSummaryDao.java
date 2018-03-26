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
package com.systematic.trading.backtest.output.file.dao.impl;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import com.systematic.trading.backtest.output.file.dao.NetWorthSummaryDao;
import com.systematic.trading.backtest.output.file.util.FileMultithreading;
import com.systematic.trading.simulation.SimulationStateListener.SimulationState;
import com.systematic.trading.simulation.analysis.networth.event.NetWorthEvent;
import com.systematic.trading.simulation.analysis.roi.CumulativeReturnOnInvestment;

/**
 * Displays the the net worth.
 * 
 * @author CJ Hare
 */
public class FileNetWorthSummaryDao implements NetWorthSummaryDao {

	private static final DecimalFormat TWO_DECIMAL_PLACES = new DecimalFormat(".##");

	private final CumulativeReturnOnInvestment cumulativeRoi;

	/** Display responsible for handling the file output. */
	private final FileMultithreading file;

	/** The last net worth recording, which makes it into the summary. */
	private NetWorthEvent lastEvent;

	public FileNetWorthSummaryDao( final CumulativeReturnOnInvestment cumulativeRoi, final FileMultithreading file ) {

		this.cumulativeRoi = cumulativeRoi;
		this.file = file;

		file.write(String.format("=== Net Worth Summary ===%n"));
	}

	@Override
	public void netWorth() {

		final BigDecimal balance = lastEvent.equityBalance();
		final BigDecimal holdingValue = lastEvent.equityBalanceValue();
		final BigDecimal cashBalance = lastEvent.cashBalance();
		final BigDecimal netWorth = lastEvent.netWorth();

		final StringBuilder output = new StringBuilder();

		output.append(String.format("Number of equities: %s%n", TWO_DECIMAL_PLACES.format(balance)));
		output.append(String.format("Holdings value: %s%n", TWO_DECIMAL_PLACES.format(holdingValue)));
		output.append(String.format("Cash account: %s%n", TWO_DECIMAL_PLACES.format(cashBalance)));
		output.append(String.format("%nTotal Net Worth: %s%n", TWO_DECIMAL_PLACES.format(netWorth)));

		// TODO this value is of dubious value, needs weighting (plus passing into summary)
		output.append(
		        String.format(
		                "%nInvestment Cumulative ROI: %s%n",
		                TWO_DECIMAL_PLACES.format(cumulativeRoi.cumulativeReturnOnInvestment())));

		file.write(output.toString());
	}

	@Override
	public void event( final NetWorthEvent event, final SimulationState state ) {

		if (SimulationState.COMPLETE == state) {
			lastEvent = event;
		}
	}
}
