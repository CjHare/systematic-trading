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
package com.systematic.trading.model.equity;

/**
 * Highest level of unique identifier for an equity within the wider simulation.
 * 
 * @author CJ Hare
 */
public class EquityIdentity {

	/** Symbol used to identify the equity in the source of the trading data. */
	private final String tickerSymbol;

	/** Number of decimal places that the equity is traded in, 0 for only whole units. */
	private final int scale;

	/**
	 * @param tickerSymbol
	 *            identity of the equity within the source of the trading data.
	 * @param scale
	 *            the number of decimal places for the units that the equity may be traded in, zero
	 *            being whole units only.
	 */
	public EquityIdentity( final String tickerSymbol, final int scale ) {

		this.tickerSymbol = tickerSymbol;
		this.scale = scale;
	}

	/**
	 * Retrieves the identity of the equity.
	 * 
	 * @return ticker symbol used to identify the equity by the trading data source.
	 */
	public String tickerSymbol() {

		return tickerSymbol;
	}

	/**
	 * The number of decimal places for the trading units.
	 * 
	 * @return the fractions of a unit that the equity may be traded in.
	 */
	public int scale() {

		return scale;
	}
}
