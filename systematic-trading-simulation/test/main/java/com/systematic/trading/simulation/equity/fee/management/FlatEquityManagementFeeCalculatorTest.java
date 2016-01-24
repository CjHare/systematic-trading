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
package com.systematic.trading.simulation.equity.fee.management;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Period;

import org.junit.Test;

import com.systematic.trading.data.price.ClosingPrice;

/**
 * Verifies behaviour of the FlatEquityManagementFeeCalculator.
 * 
 * @author CJ Hare
 */
public class FlatEquityManagementFeeCalculatorTest {

	public static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	@Test
	public void flatFeeOnlyOneShare() {
		final FlatEquityManagementFeeCalculator fee = new FlatEquityManagementFeeCalculator( new BigDecimal( 0.1 ),
				MATH_CONTEXT );

		final BigDecimal numberOfEquities = BigDecimal.valueOf( 1 );
		final ClosingPrice singleEquityValue = ClosingPrice.valueOf( BigDecimal.valueOf( 100 ) );

		final BigDecimal result = fee.calculateFee( numberOfEquities, singleEquityValue, Period.ofYears( 1 ) );

		assertEquals( .1, result.doubleValue(), 0.005 );
	}

	@Test
	public void flatFeeOnlyManyShare() {
		final FlatEquityManagementFeeCalculator fee = new FlatEquityManagementFeeCalculator( new BigDecimal( 0.1 ),
				MATH_CONTEXT );

		final BigDecimal numberOfEquities = BigDecimal.valueOf( 45.75 );
		final ClosingPrice singleEquityValue = ClosingPrice.valueOf( BigDecimal.valueOf( 100 ) );

		final BigDecimal result = fee.calculateFee( numberOfEquities, singleEquityValue, Period.ofYears( 1 ) );

		assertEquals( 4.575, result.doubleValue(), 0.005 );
	}

	@Test
	public void flatFeeOnlyManyShareManyYears() {
		final FlatEquityManagementFeeCalculator fee = new FlatEquityManagementFeeCalculator( new BigDecimal( 0.1 ),
				MATH_CONTEXT );

		final BigDecimal numberOfEquities = BigDecimal.valueOf( 45.75 );
		final ClosingPrice singleEquityValue = ClosingPrice.valueOf( BigDecimal.valueOf( 100 ) );

		final BigDecimal result = fee.calculateFee( numberOfEquities, singleEquityValue, Period.ofYears( 3 ) );

		assertEquals( 13.725, result.doubleValue(), 0.005 );
	}
}
