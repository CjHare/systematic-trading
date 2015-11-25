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
package com.systematic.trading.backtest.cash;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.MathContext;

import org.junit.Test;

import com.systematic.trading.simulation.cash.FlatInterestRate;

/**
 * Testing the flat interest rate.
 * 
 * @author CJ Hare
 */
public class FlatInterestRateTest {

	private final boolean isLeapYear = false;

	private final MathContext mc = MathContext.DECIMAL64;

	@Test
	public void zeroDaysInterest() {
		final FlatInterestRate rate = new FlatInterestRate( BigDecimal.valueOf( 7.5 ), mc );
		final int days = 0;
		final BigDecimal funds = BigDecimal.valueOf( 1000000 );

		final BigDecimal interest = rate.interest( funds, days, isLeapYear );

		assertEquals( BigDecimal.ZERO, interest );
	}

	@Test
	public void oneDaysInterest() {
		final BigDecimal interestRate = BigDecimal.valueOf( 7.5 );
		final FlatInterestRate rate = new FlatInterestRate( interestRate, mc );
		final int days = 1;
		final BigDecimal funds = BigDecimal.valueOf( 1000000 );
		final BigDecimal expectedInterest = interestRate.divide( getNonLeapYearDivisor(), mc ).multiply( funds, mc );

		final BigDecimal interest = rate.interest( funds, days, isLeapYear );

		assertEquals( expectedInterest, interest );
	}

	@Test
	public void oneDaysInterestLeapYear() {
		final BigDecimal interestRate = BigDecimal.valueOf( 7.5 );
		final FlatInterestRate rate = new FlatInterestRate( interestRate, mc );
		final int days = 1;
		final BigDecimal funds = BigDecimal.valueOf( 1000000 );
		final BigDecimal expectedInterest = interestRate.divide( getLeapYearDivisor(), mc ).multiply( funds, mc );

		final BigDecimal interest = rate.interest( funds, days, true );

		assertEquals( expectedInterest, interest );
	}

	@Test
	public void tenDaysInterest() {
		final BigDecimal interestRate = BigDecimal.valueOf( 7.5 );
		final FlatInterestRate rate = new FlatInterestRate( interestRate, mc );
		final int days = 10;
		final BigDecimal funds = BigDecimal.valueOf( 1000000 );
		final BigDecimal expectedInterest = interestRate.divide( getNonLeapYearDivisor(), mc ).multiply( funds, mc )
				.multiply( BigDecimal.valueOf( days ), mc );

		final BigDecimal interest = rate.interest( funds, days, isLeapYear );

		assertEquals( expectedInterest, interest );
	}

	@Test
	public void tenDaysInterestLeapYear() {
		final BigDecimal interestRate = BigDecimal.valueOf( 7.5 );
		final FlatInterestRate rate = new FlatInterestRate( interestRate, mc );
		final int days = 10;
		final BigDecimal funds = BigDecimal.valueOf( 1000000 );
		final BigDecimal expectedInterest = interestRate.divide( getLeapYearDivisor(), mc ).multiply( funds, mc )
				.multiply( BigDecimal.valueOf( days ), mc );

		final BigDecimal interest = rate.interest( funds, days, true );

		assertEquals( expectedInterest, interest );
	}

	private BigDecimal getNonLeapYearDivisor() {
		return BigDecimal.valueOf( 36500 );
	}

	private BigDecimal getLeapYearDivisor() {
		return BigDecimal.valueOf( 36600 );
	}
}
