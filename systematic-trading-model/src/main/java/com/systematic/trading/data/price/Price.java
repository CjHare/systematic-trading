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
package com.systematic.trading.data.price;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Price for an equity.
 * 
 * @author CJ Hare
 */
public class Price {

	/** Price of the equity. */
	private final BigDecimal amount;

	protected Price( final BigDecimal amount ) {
		this.amount = amount;
	}

	/**
	 * Creates a price from an underlying decimal value.
	 * 
	 * @param amount
	 *            decimal to create as a price, cannot be <code>null</code>.
	 * @return equivalent price for the given decimal.
	 */
	public static Price valueOf( final BigDecimal amount ) {

		if (amount == null) {
			throw new IllegalArgumentException("null is not accepted by Price.valueOf()");
		}

		return new Price(amount);
	}

	/**
	 * Retrieves the price.
	 * 
	 * @return price, never <code>null</code>.
	 */
	public BigDecimal getPrice() {

		return amount;
	}

	/**
	 * Is the price larger then another price.
	 * 
	 * @param other
	 *            comparison value.
	 * @return <code>true</code> when the other is smaller, <code>false</code> otherwise.
	 */
	public boolean isGreaterThan( final Price other ) {

		return getPrice().compareTo(other.getPrice()) > 0;
	}

	/**
	 * Is the price smaller then another price.
	 * 
	 * @param other
	 *            comparison value.
	 * @return <code>true</code> when the other value is larger, <code>false</code> otherwise.
	 */
	public boolean isLessThan( final Price other ) {

		return getPrice().compareTo(other.getPrice()) < 0;
	}

	/**
	 * Whether the price is the same as another price.
	 * 
	 * @param other
	 *            comparison value.
	 * @return <code>true</code> when the other value is the same, <code>false</code> otherwise.
	 */
	public boolean isEqaul( final Price other ) {

		return getPrice().compareTo(other.getPrice()) == 0;
	}

	/**
	 * Compares this {@code Price} with the specified {@code Price}.
	 *
	 * @param other
	 *            {@code Price} to which this {@code Price} is to be compared.
	 * @return -1, 0, or 1 as this {@code Price} is numerically less than, equal to, or greater than
	 *         {@code other}.
	 */
	public int compareTo( final Price other ) {

		return getPrice().compareTo(other.getPrice());
	}

	/**
	 * Result of subtracting the other price from this one.
	 * 
	 * @param other
	 *            the price to subtract.
	 * @param mathContext
	 *            scale, precision and rounding to apply to mathematical operations.
	 * @return the result of the subtraction, not side effecting this Price.
	 */
	public BigDecimal subtract( final Price other, final MathContext mathContext ) {

		return getPrice().subtract(other.getPrice(), mathContext);
	}
}
