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
import com.systematic.trading.simulation.analysis.networth.NetWorthEvent;
import com.systematic.trading.simulation.analysis.statistics.EventStatistics;

/**
 * Formats the comparison displays ready for output.
 * <p/>
 * 
 * @author CJ Hare
 */
public class FileNetworthComparisonDao implements NetworthComparisonDao {

	private static final DecimalFormat TWO_DECIMAL_PLACES = new DecimalFormat(".00");
	private static final DecimalFormat FOUR_DECIMAL_PLACES = new DecimalFormat(".0000");
	private static final DecimalFormat NO_DECIMAL_PLACES = new DecimalFormat("#");
	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
	private static final String COLUMN_SEPARATOR = ",";

	private final BacktestSimulationDates dates;
	private final EventStatistics statistics;
	private final BacktestBatchId batchId;
	private final FileMultithreading file;
	private final MathContext mathContext;

	public FileNetworthComparisonDao( final BacktestBatchId batchId, final BacktestSimulationDates dates,
	        final EventStatistics statistics, final FileMultithreading file, final MathContext mathContext ) {
		this.batchId = batchId;
		this.mathContext = mathContext;
		this.statistics = statistics;
		this.file = file;
		this.dates = dates;
	}

	@Override
	public void event( final NetWorthEvent event, final SimulationState state ) {

		// Only interested in the net worth when the simulation is complete
		if (SimulationState.COMPLETE == state) {
			file.write(createOutput(event));
		}
	}

	private String createOutput( final NetWorthEvent event ) {
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
		return String.format("Profit: %s", TWO_DECIMAL_PLACES.format(
		        event.getNetWorth().subtract(statistics.getCashEventStatistics().getAmountDeposited(), mathContext)));
	}

	private String deposited() {
		return String.format("Deposited: %s",
		        TWO_DECIMAL_PLACES.format(statistics.getCashEventStatistics().getAmountDeposited()));
	}

	private String cashAccount( final NetWorthEvent event ) {
		return String.format("Cash account: %s", TWO_DECIMAL_PLACES.format(event.getCashBalance()));
	}

	private String compoundAnnualGrowth( final NetWorthEvent event ) {

		final Period duration = Period.between(dates.getStartDate(), dates.getEndDate());
		final BigDecimal deposited = statistics.getCashEventStatistics().getAmountDeposited();
		final BigDecimal netWorth = event.getNetWorth();
		final BigDecimal cagr = CompoundAnnualGrowthRate.calculate(deposited, netWorth, duration.getYears(),
		        mathContext);

		return String.format("CAGR: %s", FOUR_DECIMAL_PLACES.format(cagr));
	}

	private String netWorth( final NetWorthEvent event ) {
		return String.format("Net Worth: %s", TWO_DECIMAL_PLACES.format(event.getNetWorth()));
	}

	private String equitiesHeld( final NetWorthEvent event ) {
		return String.format("Equities Held: %s", TWO_DECIMAL_PLACES.format(event.getEquityBalance()));
	}

	private String holdingsValue( final NetWorthEvent event ) {
		return String.format("Holdings value: %s", TWO_DECIMAL_PLACES.format(event.getEquityBalanceValue()));
	}

	private String exitLogic() {
		final StringJoiner out = new StringJoiner(COLUMN_SEPARATOR);
		out.add(exitOrdersPlaced());
		out.add(exitOrdersExecuted());
		out.add(exitOrdersDeleted());
		return out.toString();
	}

	private String exitOrdersDeleted() {
		return String.format("Exit orders deleted: %s", statistics.getOrderEventStatistics().getDeleteExitEventCount());
	}

	private String exitOrdersExecuted() {
		return String.format("Exit orders executed: %s", statistics.getOrderEventStatistics().getExitEventCount()
		        - statistics.getOrderEventStatistics().getDeleteExitEventCount());
	}

	private String exitOrdersPlaced() {
		return String.format("Exit orders placed: %s", statistics.getOrderEventStatistics().getExitEventCount());
	}

	private String entryLogic() {
		final StringJoiner out = new StringJoiner(COLUMN_SEPARATOR);
		out.add(minimumTradeValue());
		out.add(maximumTradeValue());
		out.add(batchId.getEntryLogic());
		out.add(entryOrdersPlaced());
		out.add(entryOrdersExecuted());
		out.add(entryOrdersDeleted());
		return out.toString();
	}

	private String maximumTradeValue() {
		return String.format("Maximum Trade: %s%s Maximum Trade Type: Absolute",
		        convertToPercetage(batchId.getMaximumTrade().getValue()), COLUMN_SEPARATOR);
	}

	private String convertToPercetage( final BigDecimal toPercentage ) {
		return String.format("%s", NO_DECIMAL_PLACES.format(toPercentage.multiply(ONE_HUNDRED)));
	}

	private String minimumTradeValue() {
		return String.format("Minimum Trade: %s%s Minimum Trade Type: Percent",
		        NO_DECIMAL_PLACES.format(batchId.getMinimumTrade().getValue()), COLUMN_SEPARATOR);
	}

	private String entryOrdersDeleted() {
		return String.format("Entry orders deleted: %s",
		        statistics.getOrderEventStatistics().getDeleteEntryEventCount());
	}

	private String entryOrdersExecuted() {
		return String.format("Entry orders executed: %s", statistics.getOrderEventStatistics().getEntryEventCount()
		        - statistics.getOrderEventStatistics().getDeleteEntryEventCount());
	}

	private String entryOrdersPlaced() {
		return String.format("Entry orders placed: %s", statistics.getOrderEventStatistics().getEntryEventCount());
	}
}