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
package com.systematic.trading.backtest.order;

import java.time.LocalDate;
import java.time.Period;

import com.systematic.trading.data.DataPoint;

/**
 * The basis of a trading order.
 * 
 * @author CJ Hare
 */
public abstract class BaseEquityOrder {

	/** The date that the entry order expires. */
	private final LocalDate expiryDate;

	/** Target price for entry order. */
	private final Price price;

	/** The number of equities, possibly a fraction for unit trusts. */
	private final EquityOrderVolume volume;

	public BaseEquityOrder( final LocalDate creationDate, final Price price, final Period expiry, final EquityOrderVolume volume ) {
		this.expiryDate = creationDate.plus( expiry );
		this.price = price;
		this.volume = volume;
	}

	public boolean isNotExpired( final DataPoint todaysTrading ) {
		return todaysTrading.getDate().isBefore( expiryDate );
	}

	public boolean areExecutionConditionsMet( final DataPoint todaysTrading ) {

		// Is the range of todays trading including the entry price
		return price.isGreaterThan( todaysTrading.getLowestPrice() )
				&& price.isLessThan( todaysTrading.getHighestPrice() );
	}

	/**
	 * Retrieves the price to execute the order at.
	 * 
	 * @return order execution price.
	 */
	protected Price getPrice() {
		return price;
	}

	/**
	 * Retrieve the number of equities to execute on.
	 * 
	 * @return the number of equities the order applies.
	 */
	protected EquityOrderVolume getVolume() {
		return volume;
	}
}
