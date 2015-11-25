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
package com.systematic.trading.simulation.brokerage;

/**
 * Highest level of unique identifier for an equity within the wider simulation.
 * 
 * @author CJ Hare
 */
public class EquityIdentity {

	/** How the equity is treated. */
	private final EquityClass type;

	/** Symbol used to identify the equity in the source of the trading data. */
	private final String tickerSymbol;

	/**
	 * @param tickerSymbol identity of the equity within the source of the trading data.
	 * @param type determines how the equity is treated.
	 */
	public EquityIdentity( final String tickerSymbol, final EquityClass type ) {
		this.tickerSymbol = tickerSymbol;
		this.type = type;
	}

	/**
	 * Retrieves the type of equity.
	 * 
	 * @return how to treat the equity.
	 */
	public EquityClass getType() {
		return type;
	}

	/**
	 * Retrieves the identity of the equity.
	 * 
	 * @return ticker symbol used to identify the equity by the trading data source.
	 */
	public String getTickerSymbol() {
		return tickerSymbol;
	}
}
