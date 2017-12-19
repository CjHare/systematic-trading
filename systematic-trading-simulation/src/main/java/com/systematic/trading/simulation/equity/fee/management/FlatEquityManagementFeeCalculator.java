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
package com.systematic.trading.simulation.equity.fee.management;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Period;

import com.systematic.trading.data.price.ClosingPrice;
import com.systematic.trading.simulation.equity.fee.EquityManagementFeeCalculator;

/**
 * The same management fee percentage is applied irrespective of the number of equities held.
 * 
 * @author CJ Hare
 */
public class FlatEquityManagementFeeCalculator implements EquityManagementFeeCalculator {

	/** Scale, precision and rounding to apply to mathematical operations. */
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL32;

	/** Annual fee, taken as a percentage of the equities under management (held). */
	private final BigDecimal annualPercentageFee;

	public FlatEquityManagementFeeCalculator( final BigDecimal annualPercentageFee ) {
		this.annualPercentageFee = annualPercentageFee;
	}

	@Override
	public BigDecimal calculate( final BigDecimal numberOfEquities, final ClosingPrice singleEquityValue,
	        final Period durationToCalculate ) {

		if (durationToCalculate.getYears() > 0) {
			final BigDecimal fee = annualPercentageFee.multiply(BigDecimal.valueOf(durationToCalculate.getYears()),
			        MATH_CONTEXT);
			return numberOfEquities.multiply(singleEquityValue.getPrice(), MATH_CONTEXT).multiply(fee, MATH_CONTEXT);
		}

		return BigDecimal.ZERO;
	}
}