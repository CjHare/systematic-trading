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
package com.systematic.trading.backtest.brokerage.fees;

import static com.systematic.trading.simulation.brokerage.BrokerageFeeUtil.EIGHT_BASIS_POINTS;
import static com.systematic.trading.simulation.brokerage.BrokerageFeeUtil.SEVENTY_FIVE_BASIS_POINTS;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.MathContext;

import org.junit.Before;
import org.junit.Test;

import com.systematic.trading.backtest.brokerage.fee.CmcMarketsBrokerageFeeStructure;
import com.systematic.trading.model.EquityClass;
import com.systematic.trading.simulation.exception.UnsupportedEquityClass;

/**
 * Test the fee structure for Bell Direct.
 * 
 * @author CJ Hare
 */
public class CmcMarketsFeeStructureTest {
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;
	public static final BigDecimal NINE_NINTY = BigDecimal.valueOf(9.9);
	public static final BigDecimal ELEVEN = BigDecimal.valueOf(10);
	private static final BigDecimal FIFTY = BigDecimal.valueOf(50);
	private static final BigDecimal FORTY = BigDecimal.valueOf(40);
	private static final BigDecimal THIRTY_SEVEN_FIFTY = BigDecimal.valueOf(37.5);

	private CmcMarketsBrokerageFeeStructure feeStructure;

	@Before
	public void setUp() {
		feeStructure = new CmcMarketsBrokerageFeeStructure(MATH_CONTEXT);
	}

	@Test
	public void firstTradeFlatFee() {
		final BigDecimal fee = calculateFee(1000, 1);

		verifyFee(ELEVEN, fee);
	}

	@Test
	public void tenthTradeFlatFee() {
		final BigDecimal fee = calculateFee(1000, 10);

		verifyFee(ELEVEN, fee);
	}

	@Test
	public void eleventhTradeFlatFee() {
		final BigDecimal fee = calculateFee(1000, 11);

		verifyFee(NINE_NINTY, fee);
	}

	@Test
	public void thirteithTradeFlatFee() {
		final BigDecimal fee = calculateFee(1000, 30);

		verifyFee(NINE_NINTY, fee);
	}

	@Test
	public void thirtyFirstTradeFlatFee() {
		final BigDecimal fee = calculateFee(1000, 31);

		verifyFee(NINE_NINTY, fee);
	}

	@Test
	public void firstTradePercentageFee() {
		final BigDecimal fee = calculateFee(50000, 1);

		verifyFee(FIFTY, fee);
	}

	@Test
	public void tenthTradePercentageFee() {
		final BigDecimal fee = calculateFee(50000, 10);

		verifyFee(FIFTY, fee);
	}

	@Test
	public void eleventhTradePercentageFee() {
		final BigDecimal fee = calculateFee(50000, 11);

		verifyFee(FORTY, fee);
	}

	//TODO change the values to have decimal points in the fee

	//TODO rename the tests, what they are testing i.e. boundaries

	@Test
	public void thirteithTradePercentageFee() {
		final BigDecimal fee = calculateFee(50000, 30);

		verifyFee(FORTY, fee);
	}

	@Test
	public void thirtyFirstTradePercentageFee() {
		final BigDecimal fee = calculateFee(50000, 31);

		verifyFee(THIRTY_SEVEN_FIFTY, fee);
	}

	@Test(expected = UnsupportedEquityClass.class)
	public void equityClassFuture() {
		feeStructure.calculateFee(BigDecimal.ZERO, EquityClass.FUTURE, 0);
	}

	@Test(expected = UnsupportedEquityClass.class)
	public void equityClassForex() {
		feeStructure.calculateFee(BigDecimal.ZERO, EquityClass.FOREX, 0);
	}

	@Test(expected = UnsupportedEquityClass.class)
	public void equityClassMetal() {
		feeStructure.calculateFee(BigDecimal.ZERO, EquityClass.METAL, 0);
	}

	private BigDecimal calculateFee( final double tradeValue, final int inclusiveNumberOfTrades ) {
		return feeStructure.calculateFee(BigDecimal.valueOf(tradeValue), EquityClass.STOCK, inclusiveNumberOfTrades);
	}

	private void verifyFee( final BigDecimal expected, final BigDecimal fee ) {
		assertEquals(String.format("%s != %s", expected, fee), 0, expected.compareTo(fee));
	}
}