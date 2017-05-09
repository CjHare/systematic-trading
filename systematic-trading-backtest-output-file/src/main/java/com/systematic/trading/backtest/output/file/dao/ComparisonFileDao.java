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
package com.systematic.trading.backtest.output.file.dao;

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
import com.systematic.trading.backtest.output.DescriptionGenerator;
import com.systematic.trading.backtest.output.file.util.FileMultithreading;
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
public class ComparisonFileDao implements NetWorthEventListener {

	private static final DecimalFormat TWO_DECIMAL_PLACES = new DecimalFormat(".00");
	private static final DecimalFormat FOUR_DECIMAL_PLACES = new DecimalFormat(".0000");

	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

	private static final DecimalFormat NO_DECIMAL_PLACES = new DecimalFormat("#");
	private static final String COLUMN_SEPARATOR = ",";
	private static final String TEXT_SEPARATOR = " ";

	/** Display responsible for handling the file output. */
	private final FileMultithreading file;

	private final MathContext mathContext;

	private final EventStatistics statistics;

	private final BacktestBootstrapConfiguration configuration;

	private final DescriptionGenerator generator = new DescriptionGenerator();

	public ComparisonFileDao( final BacktestBootstrapConfiguration configuration, final EventStatistics statistics,
	        final FileMultithreading file, final MathContext mathContext ) {
		this.configuration = configuration;
		this.mathContext = mathContext;
		this.statistics = statistics;
		this.file = file;
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

		final Period duration = Period.between(configuration.getBacktestDates().getStartDate(),
		        configuration.getBacktestDates().getEndDate());

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
		out.add(entryLogicDescription());
		out.add(entryOrdersPlaced());
		out.add(entryOrdersExecuted());
		out.add(entryOrdersDeleted());
		return out.toString();
	}

	private String entryLogicDescription() {
		final EntryLogicConfiguration entry = configuration.getEntryLogic();
		final String description;
		switch (entry.getType()) {
			case CONFIRMATION_SIGNAL:
				description = generator.entryLogicConfirmationSignal(entry);
			break;
			case PERIODIC:
				description = entryPeriodic(entry);
			break;
			case SAME_DAY_SIGNALS:
				description = entryLogicSameDaySignals(entry);
			break;
			default:
				throw new IllegalArgumentException(String.format("Unacceptable entry logic type: %s", entry.getType()));
		}

		return String.format("Entry: %s", description);
	}

	private String entryPeriodic( final EntryLogicConfiguration entry ) {
		switch (entry.getPeriodic()) {
			case WEEKLY:
				return "BuyWeekly";

			case MONTHLY:
				return "BuyMonthly";

			default:
				throw new IllegalArgumentException(String.format("Unexpected perodic: %s", entry.getPeriodic()));
		}
	}

	private String entryLogicSameDaySignals( final EntryLogicConfiguration entry ) {
		final StringJoiner out = new StringJoiner(TEXT_SEPARATOR);
		final SignalConfiguration[] signals = entry.getSameDaySignals().getSignals();
		if (signals.length == 1) {
			out.add("Signal");
		} else {
			out.add("SameDay");
		}

		for (final SignalConfiguration signal : signals) {
			out.add(signal.getDescription());
		}
		return out.toString();
	}

	private String maximumTradeValue() {
		final MaximumTrade trade = configuration.getEntryLogic().getMaximumTrade();
		return String.format("Maximum Trade: %s%s Maximum Trade Type: Absolute", convertToPercetage(trade.getValue()),
		        COLUMN_SEPARATOR);
	}

	private String minimumTradeValue() {
		final MinimumTrade trade = configuration.getEntryLogic().getMinimumTrade();
		return String.format("Minimum Trade: %s%s Minimum Trade Type: Percent",
		        NO_DECIMAL_PLACES.format(trade.getValue()), COLUMN_SEPARATOR);
	}

	private String convertToPercetage( final BigDecimal toPercentage ) {
		return String.format("%s", NO_DECIMAL_PLACES.format(toPercentage.multiply(ONE_HUNDRED)));
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