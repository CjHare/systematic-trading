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
package com.systematic.trading.maths.formula;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * The compound annual growth rate isn't a true return rate, but rather a representational figure.
 * Essentially an imaginary number describing the rate at that an investment would have grown if it
 * had grown at a steady rate (which virtually never happens in reality).
 * <p/>
 * CAGR is really just a way to smooth out an investment’s returns so that they may be more easily
 * understood.
 * 
 * @author CJ Hare
 */
public class CompoundAnnualGrowthRate {

	private static final BigDecimal ONE_HHUNDRED = BigDecimal.valueOf(100);

	/**
	 * The Compound Annual Growth Rate (CAGR) is the mean annual growth rate of an investment over a
	 * specified period of time longer than one year.
	 * <p/>
	 * To calculate compound annual growth rate, divide the value of an investment at the end of the
	 * period in question by its value at the beginning of that period, raise the result to the
	 * power of one divided by the period length, and subtract one from the subsequent result.
	 * 
	 * @param startValue beginning amount.
	 * @param finishValue amount at end of investment term.
	 * @param years number of years between the beginning value and finish value.
	 * @param mathContext context to perform mathematical operations within.
	 * @return the compound annual growth rate, as a signed percentage.
	 */
	public static BigDecimal calculate( final BigDecimal startValue, final BigDecimal finishValue, final int years,
	        final MathContext mathContext ) {

		final double change = finishValue.divide(startValue, mathContext).doubleValue();
		final double power = BigDecimal.ONE.divide(BigDecimal.valueOf(years), mathContext).doubleValue();

		return BigDecimal.valueOf(Math.pow(change, power)).subtract(BigDecimal.ONE, mathContext).multiply(ONE_HHUNDRED,
		        mathContext);
	}
}
