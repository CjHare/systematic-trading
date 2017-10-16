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

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Period;

import org.junit.Test;

import com.systematic.trading.data.price.ClosingPrice;

/**
 * Verifies behaviour of the FlatEquityManagementFeeCalculator.
 * 
 * @author CJ Hare
 */
public class FlatEquityManagementFeeCalculatorTest {

	private static final Period ELEVEN_MONTHS = Period.ofMonths(11);
	private static final Period ONE_YEAR = Period.ofYears(1);
	private static final Period FIVE_YEARS = Period.ofYears(5);

	/** Management fee being tested. */
	private FlatEquityManagementFeeCalculator fee;

	@Test
	public void oneShareUnderOneYear() {
		setUpManagementFee(0.1);

		final BigDecimal result = calculateFee(1, 100, ELEVEN_MONTHS);

		verifyFee(0, result);
	}

	@Test
	public void oneShareOneYear() {
		setUpManagementFee(0.1);

		final BigDecimal result = calculateFee(1, 100, ONE_YEAR);

		verifyFee(10, result);
	}

	@Test
	public void oneLowerValueShareOneYear() {
		setUpManagementFee(0.1);

		final BigDecimal result = calculateFee(1, 50, ONE_YEAR);

		verifyFee(5, result);
	}

	@Test
	public void oneShareOneYearHigherFee() {
		setUpManagementFee(0.2);

		final BigDecimal result = calculateFee(1, 100, ONE_YEAR);

		verifyFee(20, result);
	}

	@Test
	public void manySharesOneYear() {
		setUpManagementFee(0.1);

		final BigDecimal result = calculateFee(45.75, 100, ONE_YEAR);

		verifyFee(457.5, result);
	}

	@Test
	public void manyShareManyYears() {
		setUpManagementFee(0.1);

		final BigDecimal result = calculateFee(45.75, 100, FIVE_YEARS);

		verifyFee(2287.5, result);
	}

	private BigDecimal calculateFee( final double numberOfEquities, final double singleEquityValue,
	        final Period durationToCalculate ) {
		return fee.calculateFee(BigDecimal.valueOf(numberOfEquities),
		        ClosingPrice.valueOf(BigDecimal.valueOf(singleEquityValue)), durationToCalculate);
	}

	private void setUpManagementFee( final double annualPercentageFee ) {
		fee = new FlatEquityManagementFeeCalculator(new BigDecimal(annualPercentageFee));
	}

	private void verifyFee( final double expected, final BigDecimal actual ) {
		assertEquals(String.format("%s != %s", expected, actual), 0,
		        BigDecimal.valueOf(expected).compareTo(actual.setScale(3, RoundingMode.HALF_EVEN)));
	}
}