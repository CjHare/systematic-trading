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
package com.systematic.trading.maths.formula;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * A facade encapsulate precision calculations regarding the Networth calculation.
 * 
 * @author CJ Hare
 */
public class Networth {

	/** Used for the conversion to percentage. */
	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

	/** Scale, precision and rounding to apply to mathematical operations. */
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL32;

	/** Running total of net worth. */
	private BigDecimal totalNetworth = BigDecimal.ZERO;

	public void add( final BigDecimal value ) {

		totalNetworth = totalNetworth.add(value, MATH_CONTEXT);
	}

	/**
	 * Adds the value of a quantity of an equity at the close of day price.
	 * 
	 * @param quantity
	 *            number of the equity to add.
	 * @param price
	 *            value of the equity.
	 */
	public void addEquity( final BigDecimal quantity, final BigDecimal price ) {

		add(quantity.multiply(price, MATH_CONTEXT));
	}

	public void reset() {

		totalNetworth = BigDecimal.ZERO;
	}

	public BigDecimal networth() {

		return totalNetworth;
	}

	/**
	 * Percentage increase or decrease between here and the endValue.
	 * 
	 * @param endNetworth
	 *            the end net worth to calculate the percentage increase to obtain.
	 * @param adjustment
	 *            value to subtract from the change in net worth values increase.
	 * @return percentage channge to get from this net worth to the end value, factoring in the
	 *         adjustment.
	 */
	public BigDecimal percentageChange( final Networth endNetworth, final Networth adjustment ) {

		// Difference / previous worth
		final BigDecimal absoluteChange = endNetworth.networth().subtract(totalNetworth, MATH_CONTEXT)
		        .subtract(adjustment.networth(), MATH_CONTEXT);

		if (BigDecimal.ZERO.compareTo(absoluteChange) == 0) { return BigDecimal.ZERO; }

		return absoluteChange.divide(totalNetworth, MATH_CONTEXT).multiply(ONE_HUNDRED, MATH_CONTEXT);
	}
}
