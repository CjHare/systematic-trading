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
 * CMC Markets has three pricing tiers based on the number of trades in that month
 *  1st)  Trade # 0 - 10 : $11 or 0.10%, whichever is the greater
 *  2nd) Trade # 11 - 30 : $9.90 or 0.08%, whichever greater
 *  3rd)    Trade # 31 + : All trades are $9.90 or 0.075%, whichever greater
 * 
 * @author CJ Hare
 */
public class CmcMarketsFeeStructureTest {

	private CmcMarketsBrokerageFeeStructure feeStructure;

	@Before
	public void setUp() {
		feeStructure = new CmcMarketsBrokerageFeeStructure(MathContext.DECIMAL64);
	}

	@Test
	public void firstTieratFee() {
		final BigDecimal fee = calculateFee(1000, 1);

		verifyFee(11, fee);
	}

	@Test
	public void firstTierEdgeFlatFee() {
		final BigDecimal fee = calculateFee(1000, 10);

		verifyFee(11, fee);
	}

	@Test
	public void secondTierFlatFee() {
		final BigDecimal fee = calculateFee(1000, 11);

		verifyFee(9.9, fee);
	}

	@Test
	public void secondTierEdgeFlatFee() {
		final BigDecimal fee = calculateFee(1000, 30);

		verifyFee(9.9, fee);
	}

	@Test
	public void thirdTierFlatFee() {
		final BigDecimal fee = calculateFee(1000, 31);

		verifyFee(9.9, fee);
	}

	@Test
	public void firstTierPercentageFee() {
		final BigDecimal fee = calculateFee(54321, 1);

		verifyFee(54.321, fee);
	}

	@Test
	public void firstTierEdgePercentageFee() {
		final BigDecimal fee = calculateFee(50000, 10);

		verifyFee(50, fee);
	}

	@Test
	public void secondTierPercentageFee() {
		final BigDecimal fee = calculateFee(50000, 11);

		verifyFee(40, fee);
	}

	@Test
	public void secondTierEdgePercentageFee() {
		final BigDecimal fee = calculateFee(50000, 30);

		verifyFee(40, fee);
	}

	@Test
	public void thirdTierPercentageFee() {
		final BigDecimal fee = calculateFee(50000, 31);

		verifyFee(37.5, fee);
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

	private void verifyFee( final double expected, final BigDecimal fee ) {
		assertEquals(String.format("Expected of %s != Fee of %s", expected, fee), 0,
		        BigDecimal.valueOf(expected).compareTo(fee));
	}
}