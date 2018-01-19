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
import java.math.MathContext;
import java.text.DecimalFormat;
import java.time.Period;
import java.util.StringJoiner;

import com.systematic.trading.backtest.BacktestBatchId;
import com.systematic.trading.backtest.BacktestSimulationDates;
import com.systematic.trading.backtest.output.file.dao.NetworthComparisonDao;
import com.systematic.trading.backtest.output.file.util.FileMultithreading;
import com.systematic.trading.maths.formula.CompoundAnnualGrowthRate;
import com.systematic.trading.simulation.SimulationStateListener.SimulationState;
import com.systematic.trading.simulation.analysis.networth.event.NetWorthEvent;
import com.systematic.trading.simulation.analysis.statistics.EventStatistics;

/**
 * Formats the comparison displays ready for output.
 * <p/>
 * 
 * @author CJ Hare
 */
public class FileNetworthComparisonDao implements NetworthComparisonDao {

	/** Scale, precision and rounding to apply to mathematical operations. */
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL32;

	private static final DecimalFormat TWO_DECIMAL_PLACES = new DecimalFormat(".00");
	private static final DecimalFormat FOUR_DECIMAL_PLACES = new DecimalFormat(".0000");
	private static final String COLUMN_SEPARATOR = ",";

	private final CompoundAnnualGrowthRate compoundAnnualGrowthRate = new CompoundAnnualGrowthRate();
	private final BacktestSimulationDates dates;
	private final EventStatistics statistics;
	private final BacktestBatchId batchId;
	private final FileMultithreading file;

	public FileNetworthComparisonDao( final BacktestBatchId batchId, final BacktestSimulationDates dates,
	        final EventStatistics statistics, final FileMultithreading file ) {

		this.batchId = batchId;
		this.statistics = statistics;
		this.file = file;
		this.dates = dates;
	}

	@Override
	public void event( final NetWorthEvent event, final SimulationState state ) {

		// Only interested in the net worth when the simulation is complete
		if (SimulationState.COMPLETE == state) {
			file.write(output(event));
		}
	}

	private String output( final NetWorthEvent event ) {

		final StringJoiner out = new StringJoiner(COLUMN_SEPARATOR);
		out.add(compoundAnnualGrowth(event));
		out.add(netWorth(event));
		out.add(equitiesHeld(event));
		out.add(holdingsValue(event));
		out.add(cashAccount(event));
		out.add(deposited());
		out.add(profit(event));
		out.add(entryLogic());
		out.add(exitLogic());
		return String.format("%s%n", out.toString());
	}

	private String profit( final NetWorthEvent event ) {

		return String.format("Profit: %s", TWO_DECIMAL_PLACES
		        .format(event.netWorth().subtract(statistics.cashEventStatistics().amountDeposited(), MATH_CONTEXT)));
	}

	private String deposited() {

		return String.format("Deposited: %s",
		        TWO_DECIMAL_PLACES.format(statistics.cashEventStatistics().amountDeposited()));
	}

	private String cashAccount( final NetWorthEvent event ) {

		return String.format("Cash account: %s", TWO_DECIMAL_PLACES.format(event.cashBalance()));
	}

	private String compoundAnnualGrowth( final NetWorthEvent event ) {

		final Period duration = Period.between(dates.startDate(), dates.endDate());
		final BigDecimal deposited = statistics.cashEventStatistics().amountDeposited();
		final BigDecimal netWorth = event.netWorth();
		final BigDecimal cagr = compoundAnnualGrowthRate.calculate(deposited, netWorth, duration.getYears());

		return String.format("CAGR: %s", FOUR_DECIMAL_PLACES.format(cagr));
	}

	private String netWorth( final NetWorthEvent event ) {

		return String.format("Net Worth: %s", TWO_DECIMAL_PLACES.format(event.netWorth()));
	}

	private String equitiesHeld( final NetWorthEvent event ) {

		return String.format("Equities Held: %s", TWO_DECIMAL_PLACES.format(event.equityBalance()));
	}

	private String holdingsValue( final NetWorthEvent event ) {

		return String.format("Holdings value: %s", TWO_DECIMAL_PLACES.format(event.equityBalanceValue()));
	}

	private String exitLogic() {

		final StringJoiner out = new StringJoiner(COLUMN_SEPARATOR);
		out.add(exitOrdersPlaced());
		out.add(exitOrdersExecuted());
		out.add(exitOrdersDeleted());
		return out.toString();
	}

	private String exitOrdersDeleted() {

		return String.format("Exit orders deleted: %s", statistics.orderEventStatistics().deleteExitEventCount());
	}

	private String exitOrdersExecuted() {

		return String.format("Exit orders executed: %s", statistics.orderEventStatistics().exitEventCount()
		        - statistics.orderEventStatistics().deleteExitEventCount());
	}

	private String exitOrdersPlaced() {

		return String.format("Exit orders placed: %s", statistics.orderEventStatistics().exitEventCount());
	}

	private String entryLogic() {

		final StringJoiner out = new StringJoiner(COLUMN_SEPARATOR);
		out.add(batchId.name());
		out.add(entryOrdersPlaced());
		out.add(entryOrdersExecuted());
		out.add(entryOrdersDeleted());
		return out.toString();
	}

	private String entryOrdersDeleted() {

		return String.format("Entry orders deleted: %s", statistics.orderEventStatistics().deleteEntryEventCount());
	}

	private String entryOrdersExecuted() {

		return String.format("Entry orders executed: %s", statistics.orderEventStatistics().entryEventCount()
		        - statistics.orderEventStatistics().deleteEntryEventCount());
	}

	private String entryOrdersPlaced() {

		return String.format("Entry orders placed: %s", statistics.orderEventStatistics().entryEventCount());
	}
}