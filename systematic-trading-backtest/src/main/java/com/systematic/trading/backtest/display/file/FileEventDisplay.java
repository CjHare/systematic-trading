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

import java.text.DecimalFormat;
import java.time.temporal.ChronoUnit;

import com.systematic.trading.model.TickerSymbolTradingData;
import com.systematic.trading.simulation.brokerage.event.BrokerageEvent;
import com.systematic.trading.simulation.brokerage.event.BrokerageEventListener;
import com.systematic.trading.simulation.cash.event.CashEvent;
import com.systematic.trading.simulation.cash.event.CashEventListener;
import com.systematic.trading.simulation.order.event.OrderEvent;
import com.systematic.trading.simulation.order.event.OrderEventListener;

/**
 * Simple output to the console for the events.
 * 
 * @author CJ Hare
 */
public class FileEventDisplay implements CashEventListener, OrderEventListener, BrokerageEventListener {

	private static final DecimalFormat TWO_DECIMAL_PLACES = new DecimalFormat( ".##" );

	private final CashEventListener cashEventListener;
	private final OrderEventListener orderEventListener;
	private final BrokerageEventListener brokerageEventListener;

	public FileEventDisplay( final TickerSymbolTradingData tradingData, final FileDisplayMultithreading display ) {

		display.write( createHeaderOutput( tradingData ) );

		this.cashEventListener = new FileCashEventDisplay( display );
		this.orderEventListener = new FileOrderEventDisplay( display );
		this.brokerageEventListener = new FileBrokerageEventDisplay( display );

	}

	private String createHeaderOutput( final TickerSymbolTradingData tradingData ) {

		final StringBuilder output = new StringBuilder();
		output.append( "\n" );
		output.append( "#######################\n" );
		output.append( "### Backtest Events ###\n" );
		output.append( "#######################\n" );
		output.append( "\n" );

		output.append(
				String.format( "Data set for %s from %s to %s\n", tradingData.getEquityIdentity().getTickerSymbol(),
						tradingData.getStartDate(), tradingData.getEndDate() ) );

		final long daysBetween = ChronoUnit.DAYS.between( tradingData.getStartDate(), tradingData.getEndDate() );
		final double percentageTradingDays = ((double) tradingData.getNumberOfTradingDays() / daysBetween) * 100;

		output.append( String.format( "# trading days: %s over %s days (%s percentage trading days)\n",
				tradingData.getNumberOfTradingDays(), daysBetween,
				TWO_DECIMAL_PLACES.format( percentageTradingDays ) ) );

		output.append( "\n" );

		return output.toString();
	}

	@Override
	public void event( final BrokerageEvent event ) {
		brokerageEventListener.event( event );
	}

	@Override
	public void event( final OrderEvent event ) {
		orderEventListener.event( event );
	}

	@Override
	public void event( final CashEvent event ) {
		cashEventListener.event( event );
	}
}
