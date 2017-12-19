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
package com.systematic.trading.backtest.brokerage.fee;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Before;
import org.junit.Test;

import com.systematic.trading.backtest.brokerage.fee.VanguardBrokerageFees;
import com.systematic.trading.model.EquityClass;
import com.systematic.trading.simulation.exception.UnsupportedEquityClass;

/**
 * Test the fee structure for Vanguard Retail fund, .10% buy / sell spread.
 * 
 * @author CJ Hare
 */
public class VanguardFeeStructureTest {

	private VanguardBrokerageFees feeStructure;

	@Before
	public void setUp() {
		feeStructure = new VanguardBrokerageFees();
	}

	@Test
	public void trade() {
		final BigDecimal fee = calculateFee(1234.5, 1);

		verifyFee(0.9876, fee);
	}

	@Test
	public void tradeRandomNumberOfPreviousTrades() {
		final BigDecimal fee = calculateFee(12345.67, ThreadLocalRandom.current().nextInt(20000));

		verifyFee(9.876536, fee);
	}

	@Test(expected = UnsupportedEquityClass.class)
	public void equityClassFuture() {
		feeStructure.cost(BigDecimal.ZERO, EquityClass.FUTURE, 0);
	}

	@Test(expected = UnsupportedEquityClass.class)
	public void equityClassForex() {
		feeStructure.cost(BigDecimal.ZERO, EquityClass.FOREX, 0);
	}

	@Test(expected = UnsupportedEquityClass.class)
	public void equityClassMetal() {
		feeStructure.cost(BigDecimal.ZERO, EquityClass.METAL, 0);
	}

	private BigDecimal calculateFee( final double tradeValue, final int inclusiveNumberOfTrades ) {
		return feeStructure.cost(BigDecimal.valueOf(tradeValue), EquityClass.STOCK, inclusiveNumberOfTrades);
	}

	private void verifyFee( final double expected, final BigDecimal fee ) {
		assertEquals(String.format("Expected of %s != Fee of %s", expected, fee), 0,
		        BigDecimal.valueOf(expected).compareTo(fee));
	}
}