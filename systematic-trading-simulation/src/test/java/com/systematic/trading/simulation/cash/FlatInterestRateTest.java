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
package com.systematic.trading.simulation.cash;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.MathContext;

import org.junit.Before;
import org.junit.Test;

import com.systematic.trading.simulation.cash.FlatInterestRate;

/**
 * Testing the flat interest rate.
 * 
 * @author CJ Hare
 */
public class FlatInterestRateTest {

	private static final BigDecimal FUNDS = BigDecimal.valueOf(1000000);

	private FlatInterestRate rate;

	@Before
	public void setUp() {

		rate = new FlatInterestRate(BigDecimal.valueOf(7.5), MathContext.DECIMAL64);
	}

	@Test
	public void zeroDaysInterest() {

		final BigDecimal interest = interest(0);

		verifyInterest(0, interest);
	}

	@Test
	public void oneDaysInterest() {

		final BigDecimal interest = interest(1);

		verifyInterest(205.4794520547945, interest);
	}

	@Test
	public void oneDaysInterestLeapYear() {

		final BigDecimal interest = interestLeapYear(1);

		verifyInterest(204.9180327868852, interest);
	}

	@Test
	public void tenDaysInterest() {

		final BigDecimal interest = interest(10);

		verifyInterest(2054.794520547945, interest);
	}

	@Test
	public void tenDaysInterestLeapYear() {

		final BigDecimal interest = interestLeapYear(10);

		verifyInterest(2049.180327868852, interest);
	}

	private BigDecimal interest( final int days ) {

		return rate.interest(FUNDS, days, false);
	}

	private BigDecimal interestLeapYear( final int days ) {

		return rate.interest(FUNDS, days, true);
	}

	private void verifyInterest( final double expected, final BigDecimal interest ) {

		assertEquals(String.format("Expected %s != %s", expected, interest), 0,
		        BigDecimal.valueOf(expected).compareTo(interest));
	}
}