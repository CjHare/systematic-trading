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
package com.systematic.trading.data.price;

import java.math.BigDecimal;

/**
 * Price for an equity.
 * 
 * @author CJ Hare
 */
public class Price {

	/**
	 * Creates a price from an underlying decimal value.
	 * 
	 * @param price decimal to create as a price, cannot be <code>null</code>.
	 * @return equivalent price for the given decimal.
	 */
	public static Price valueOf( final BigDecimal price ) {
		if (price == null) {
			throw new IllegalArgumentException( "null is not accepted by Price.valueOf()" );
		}

		return new Price( price );
	}

	/** Price of the equity. */
	private final BigDecimal price;

	protected Price( final BigDecimal price ) {
		this.price = price;
	}

	/**
	 * Retrieves the price.
	 * 
	 * @return price, never <code>null</code>.
	 */
	public BigDecimal getPrice() {
		return price;
	}

	/**
	 * Is the price larger then another price.
	 * 
	 * @param other comparison value.
	 * @return <code>true</code> when the other is smaller, <code>false</code> otherwise.
	 */
	public boolean isGreaterThan( final Price other ) {
		return price.compareTo( other.getPrice() ) > 0;
	}

	/**
	 * Is the price smaller then another price.
	 * 
	 * @param other comparison value.
	 * @return <code>true</code> when the other value is larger, <code>false</code> otherwise.
	 */
	public boolean isLessThan( final Price other ) {
		return price.compareTo( other.getPrice() ) < 0;
	}
}
