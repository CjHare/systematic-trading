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
package com.systematic.trading.backtest.display.file;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.time.Period;
import java.util.StringJoiner;

import com.systematic.trading.backtest.configuration.BacktestBootstrapConfiguration;
import com.systematic.trading.backtest.configuration.entry.EntryLogicConfiguration;
import com.systematic.trading.backtest.configuration.signals.SignalConfiguration;
import com.systematic.trading.backtest.configuration.trade.MaximumTrade;
import com.systematic.trading.backtest.configuration.trade.MinimumTrade;
import com.systematic.trading.maths.formula.CompoundAnnualGrowthRate;
import com.systematic.trading.simulation.SimulationStateListener.SimulationState;
import com.systematic.trading.simulation.analysis.networth.NetWorthEvent;
import com.systematic.trading.simulation.analysis.networth.NetWorthEventListener;
import com.systematic.trading.simulation.analysis.statistics.EventStatistics;

/**
 * Persists the comparison displays into a file.
 * <p/>
 * Assumption is the directory is already empty.
 * 
 * @author CJ Hare
 */
public class FileComparisonDisplay implements NetWorthEventListener {

	private static final DecimalFormat TWO_DECIMAL_PLACES = new DecimalFormat(".00");

	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

	private static final DecimalFormat MAX_TWO_DECIMAL_PLACES = new DecimalFormat("#");
	private static final String SEPARATOR = ",";

	/** Display responsible for handling the file output. */
	private final FileDisplayMultithreading display;

	private final MathContext mathContext;

	private final EventStatistics statistics;

	private final BacktestBootstrapConfiguration configuration;

	public FileComparisonDisplay(final BacktestBootstrapConfiguration configuration, final EventStatistics statistics,
	        final FileDisplayMultithreading display, final MathContext mathContext) {
		this.configuration = configuration;
		this.mathContext = mathContext;
		this.statistics = statistics;
		this.display = display;
	}

	@Override
	public void event( final NetWorthEvent event, final SimulationState state ) {

		// Only interested in the net worth when the simulation is complete
		if (SimulationState.COMPLETE == state) {
			display.write(createOutput(event));
		}
	}

	private String createOutput( final NetWorthEvent event ) {
		final StringJoiner out = new StringJoiner(SEPARATOR);
		out.add(compoundAnnualGrowth(event));
		out.add(netWorth(event));
		out.add(equitiesHeld(event));
		out.add(holdingsValue(event));
		out.add(cashAccount(event));
		out.add(deposited(event));
		out.add(profit(event));
		out.add(entryLogic(event));
		out.add(exitLogic(event));
		return String.format("%s%n", out.toString());
	}

	private String profit( final NetWorthEvent event ) {
		return String.format("Profit: %s", TWO_DECIMAL_PLACES.format(
		        event.getNetWorth().subtract(statistics.getCashEventStatistics().getAmountDeposited(), mathContext)));
	}

	private String deposited( final NetWorthEvent event ) {
		return String.format("Deposited: %s",
		        TWO_DECIMAL_PLACES.format(statistics.getCashEventStatistics().getAmountDeposited()));
	}

	private String cashAccount( final NetWorthEvent event ) {
		return String.format("Cash account: %s", TWO_DECIMAL_PLACES.format(event.getCashBalance()));
	}

	private String compoundAnnualGrowth( final NetWorthEvent event ) {

		final Period duration = Period.between(configuration.getBacktestDates().getStartDate(),
		        configuration.getBacktestDates().getEndDate());

		final BigDecimal deposited = statistics.getCashEventStatistics().getAmountDeposited();
		final BigDecimal netWorth = event.getNetWorth();
		final BigDecimal cagr = CompoundAnnualGrowthRate.calculate(deposited, netWorth, duration.getYears(),
		        mathContext);

		return String.format("CAGR: %s", cagr);
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

	private String exitLogic( final NetWorthEvent event ) {
		final StringJoiner out = new StringJoiner(SEPARATOR);
		out.add(exitOrdersPlaced(event));
		out.add(exitOrdersExecuted(event));
		out.add(exitOrdersDeleted(event));
		return out.toString();
	}

	private String exitOrdersDeleted( final NetWorthEvent event ) {
		return String.format("Exit orders deleted: %s", statistics.getOrderEventStatistics().getDeleteExitEventCount());
	}

	private String exitOrdersExecuted( final NetWorthEvent event ) {
		return String.format("Exit orders executed: %s", statistics.getOrderEventStatistics().getExitEventCount()
		        - statistics.getOrderEventStatistics().getDeleteExitEventCount());
	}

	private String exitOrdersPlaced( final NetWorthEvent event ) {
		return String.format("Exit orders placed: %s", statistics.getOrderEventStatistics().getExitEventCount());
	}

	private String entryLogic( final NetWorthEvent event ) {
		final StringJoiner out = new StringJoiner(SEPARATOR);
		out.add(minimumTradeValue());
		out.add(maximumTradeValue());
		out.add(entryLogicDescription());
		out.add(entryOrdersPlaced(event));
		out.add(entryOrdersExecuted(event));
		out.add(entryOrdersDeleted(event));
		return out.toString();
	}

	private String entryLogicDescription() {
		final EntryLogicConfiguration entry = configuration.getEntryLogic();
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

	private String entryLogicConfirmationSignal( final EntryLogicConfiguration entry ) {
		final int delay = entry.getConfirmationSignal().getType().getDelayUntilConfirmationRange();
		final int range = entry.getConfirmationSignal().getType().getConfirmationDayRange();
		final StringJoiner out = new StringJoiner(SEPARATOR);
		out.add(entry.getConfirmationSignal().getAnchor().getDescription());
		out.add("confirmedBy");
		out.add(entry.getConfirmationSignal().getConfirmation().getDescription());
		out.add("in");
		out.add(String.valueOf(delay));
		out.add("to");
		out.add(String.valueOf(delay + range));
		out.add("days");
		return out.toString();
	}

	private String entryLogicSameDaySignals( final EntryLogicConfiguration entry ) {
		final StringJoiner out = new StringJoiner(SEPARATOR);
		out.add("SameDay");
		for (final SignalConfiguration signal : entry.getSameDaySignals().getSignals()) {
			out.add(signal.getDescription());
		}
		return out.toString();
	}

	private String maximumTradeValue() {
		final MaximumTrade trade = configuration.getEntryLogic().getMaximumTrade();
		return String.format("Maximum Trade: %s%s Maximum Trade Type: Absolute", convertToPercetage(trade.getValue()),
		        SEPARATOR);
	}

	private String minimumTradeValue() {
		final MinimumTrade trade = configuration.getEntryLogic().getMinimumTrade();
		return String.format("Minimum Trade: %s%s Minimum Trade Type: Percent",
		        MAX_TWO_DECIMAL_PLACES.format(trade.getValue()), SEPARATOR);
	}

	private String convertToPercetage( final BigDecimal toPercentage ) {
		return String.format("%s", MAX_TWO_DECIMAL_PLACES.format(toPercentage.multiply(ONE_HUNDRED)));
	}

	private String entryOrdersDeleted( final NetWorthEvent event ) {
		return String.format("Entry orders deleted: %s",
		        statistics.getOrderEventStatistics().getDeleteEntryEventCount());
	}

	private String entryOrdersExecuted( final NetWorthEvent event ) {
		return String.format("Entry orders executed: %s", statistics.getOrderEventStatistics().getEntryEventCount()
		        - statistics.getOrderEventStatistics().getDeleteEntryEventCount());
	}

	private String entryOrdersPlaced( final NetWorthEvent event ) {
		return String.format("Entry orders placed: %s", statistics.getOrderEventStatistics().getEntryEventCount());
	}
}