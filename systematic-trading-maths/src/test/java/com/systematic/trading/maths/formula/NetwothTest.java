/**
 * Copyright (c) 2015-2017, CJ Hare
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of [project] nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.systematic.trading.maths.formula;

import static com.systematic.trading.maths.util.SystematicTradingMathsAssert.assertBigDecimalEquals;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

/**
 * Verify the net worth operation and precision.
 * 
 * @author CJ Hare
 */
public class NetwothTest {

	private Networth networth;

	@Before
	public void setUp() {
		networth = new Networth();
	}

	@Test
	public void get() {
		assertBigDecimalEquals(0, networth.get());
	}

	@Test
	public void reset() {
		networth.add(BigDecimal.ONE);

		networth.reset();

		assertBigDecimalEquals(0, networth.get());
	}

	@Test
	public void add() {
		final double value = 1.234567;

		networth.add(BigDecimal.valueOf(value));

		assertBigDecimalEquals(value, networth.get());
	}

	@Test
	public void addEquity() {
		final double value = 1.234567;
		final double price = 9876.21;

		networth.addEquity(BigDecimal.valueOf(value), BigDecimal.valueOf(price));

		assertBigDecimalEquals(12192.84, networth.get());
	}

	@Test
	public void addTwoValues() {
		final double firstValue = 1.234567;
		final double secondValue = 34.234567;

		networth.add(BigDecimal.valueOf(firstValue));
		networth.add(BigDecimal.valueOf(secondValue));

		assertBigDecimalEquals(35.46913, networth.get());
	}

	@Test
	public void percetageChangeNone() {
		final Networth endNetworth = new Networth();
		final Networth adjustment = new Networth();

		final BigDecimal change = networth.percentageChange(endNetworth, adjustment);

		assertBigDecimalEquals(0, change);
	}

	@Test
	public void percetageChangeNoneWithAdjustment() {
		final Networth endNetworth = new Networth();
		final Networth adjustment = new Networth();
		endNetworth.add(BigDecimal.ONE);
		adjustment.add(BigDecimal.ONE);

		final BigDecimal change = networth.percentageChange(endNetworth, adjustment);

		assertBigDecimalEquals(0, change);
	}

	@Test
	public void percetageChange() {
		final Networth endNetworth = new Networth();
		final Networth adjustment = new Networth();
		endNetworth.add(BigDecimal.valueOf(3.1234567));
		adjustment.add(BigDecimal.ONE);
		networth.add(BigDecimal.ONE);

		final BigDecimal change = networth.percentageChange(endNetworth, adjustment);

		assertBigDecimalEquals(112.3457, change);
	}
}