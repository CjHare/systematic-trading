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
package com.systematic.trading.maths.formula;

import static com.systematic.trading.maths.util.SystematicTradingMathsAssert.assertBigDecimalEquals;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

/**
 * Verifies behaviour for CompoundAnnualGrowthRate.
 * 
 * @author CJ Hare
 */
public class CompoundAnnualGrowthRateTest {

	/** CAGR calculator being tested. */
	private CompoundAnnualGrowthRate calculator;

	@Before
	public void setUp() {
		calculator = new CompoundAnnualGrowthRate();
	}

	@Test
	public void cagrTenPercent() {
		final BigDecimal startValue = BigDecimal.ONE;
		final BigDecimal endValue = BigDecimal.valueOf(1.21);
		final int years = 2;

		final BigDecimal cagr = calculate(startValue, endValue, years);

		assertBigDecimalEquals(10.0, cagr);
	}

	public void cagrOnePercent() {
		final BigDecimal startValue = BigDecimal.ONE;
		final BigDecimal endValue = BigDecimal.valueOf(1.01);
		final int years = 1;

		final BigDecimal cagr = calculate(startValue, endValue, years);

		assertBigDecimalEquals(1.0, cagr);
	}

	private BigDecimal calculate( final BigDecimal startValue, final BigDecimal finishValue, final int years ) {
		return calculator.calculate(startValue, finishValue, years);
	}
}