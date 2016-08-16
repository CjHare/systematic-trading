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

import com.systematic.trading.maths.formula.CompoundAnnualGrowthRate;
import com.systematic.trading.simulation.SimulationStateListener.SimulationState;
import com.systematic.trading.simulation.analysis.networth.NetWorthEvent;
import com.systematic.trading.simulation.analysis.networth.NetWorthEventListener;
import com.systematic.trading.simulation.analysis.statistics.EventStatistics;
import com.systematic.trading.simulation.logic.EntryLogic;
import com.systematic.trading.simulation.logic.trade.TradeValueCalculator;

/**
 * Persists the comparison displays into a file.
 * <p/>
 * Assumption is the directory is already empty.
 * 
 * @author CJ Hare
 */
public class FileComparisonDisplay implements NetWorthEventListener {

	private static final DecimalFormat TWO_DECIMAL_PLACES = new DecimalFormat(".00");

	private static final String SEPARATOR = ",";

	/** Display responsible for handling the file output. */
	private final FileDisplayMultithreading display;

	private final MathContext mathContext;

	private final EventStatistics statistics;

	private final EntryLogic entryLogic;

	/** The period of time that the simulation covered. */
	private final Period duration;

	public FileComparisonDisplay(final Period duration, final EventStatistics statistics, final EntryLogic entryLogic,
	        final FileDisplayMultithreading display, final MathContext mathContext) {
		this.entryLogic = entryLogic;
		this.mathContext = mathContext;
		this.statistics = statistics;
		this.duration = duration;
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
		final StringJoiner out = new StringJoiner(",");
		out.add(compoundAnnualGrowth(event));
		out.add(netWorth(event));
		out.add(equitiesHeld(event));
		out.add(holdingsValue(event));
		out.add(cashAccount(event));
		out.add(deposited(event));
		out.add(profit(event));

		out.add(minimumTradeValue());
		out.add(maximumTradeValue());
		//TODO add filters used

		out.add(entryOrdersPlaced(event));
		out.add(entryOrdersExecuted(event));
		out.add(entryOrdersDeleted(event));
		out.add(exitOrdersPlaced(event));
		out.add(exitOrdersExecuted(event));
		out.add(exitOrdersDeleted(event));

		//TODO remove the general description
		out.add(event.getDescription());

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

	private String maximumTradeValue() {
		return tradeValue("Maximum", entryLogic.getTradeValue().getMinimumValue());
	}

	private String minimumTradeValue() {
		return tradeValue("Minimum", entryLogic.getTradeValue().getMinimumValue());
	}

	private String tradeValue( final String prefix, final TradeValueCalculator tradeValue ) {
		return String.format("%s Trade Value: %s%s Trade Type: %s", prefix, tradeValue.getValue(), SEPARATOR, prefix,
		        tradeValue.getType());
	}
}