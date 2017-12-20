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
 * Verify the net worth operation and precision.
 * 
 * @author CJ Hare
 */
public class NetwothTest {

	/** Instance being tested, */
	private Networth networth;

	@Before
	public void setUp() {

		networth = new Networth();
	}

	@Test
	public void get() {

		assertBigDecimalEquals(0, getNetworth());
	}

	@Test
	public void reset() {

		addValue(1);

		networth.reset();

		assertBigDecimalEquals(0, getNetworth());
	}

	@Test
	public void add() {

		final double value = 1.234567;

		addValue(value);

		assertBigDecimalEquals(value, getNetworth());
	}

	@Test
	public void addEquity() {

		addEquity(1.234567, 9876.21);

		assertBigDecimalEquals(12192.84, getNetworth());
	}

	@Test
	public void addTwoValues() {

		addValue(1.234567);
		addValue(34.234567);

		assertBigDecimalEquals(35.46913, getNetworth());
	}

	@Test
	public void percetageChangeNone() {

		final Networth endNetworth = setUpNetworth(0);
		final Networth adjustment = setUpNetworth(0);

		final BigDecimal change = percentageChange(endNetworth, adjustment);

		assertBigDecimalEquals(0, change);
	}

	@Test
	public void percetageChangeNoneWithAdjustment() {

		final Networth endNetworth = setUpNetworth(1);
		final Networth adjustment = setUpNetworth(1);

		final BigDecimal change = percentageChange(endNetworth, adjustment);

		assertBigDecimalEquals(0, change);
	}

	@Test
	public void percetageChange() {

		final Networth endNetworth = setUpNetworth(3.1234567);
		final Networth adjustment = setUpNetworth(1);
		addValue(1);

		final BigDecimal change = percentageChange(endNetworth, adjustment);

		assertBigDecimalEquals(112.3457, change);
	}

	private BigDecimal getNetworth() {

		return networth.get();
	}

	private BigDecimal percentageChange( final Networth endNetworth, final Networth adjustment ) {

		return networth.percentageChange(endNetworth, adjustment);
	}

	private Networth setUpNetworth( final double networth ) {

		final Networth worth = new Networth();
		worth.add(BigDecimal.valueOf(networth));
		return worth;
	}

	private void addValue( final double value ) {

		networth.add(BigDecimal.valueOf(value));
	}

	private void addEquity( final double quantity, final double price ) {

		networth.addEquity(BigDecimal.valueOf(quantity), BigDecimal.valueOf(price));
	}
}