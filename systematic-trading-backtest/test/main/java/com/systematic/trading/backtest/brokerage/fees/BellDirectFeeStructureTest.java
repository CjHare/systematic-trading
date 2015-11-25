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
package com.systematic.trading.backtest.brokerage.fees;

import static com.systematic.trading.simulation.brokerage.BrokerageFeeUtil.EIGHT_BASIS_POINTS;
import static com.systematic.trading.simulation.brokerage.BrokerageFeeUtil.FIFTEEN;
import static com.systematic.trading.simulation.brokerage.BrokerageFeeUtil.TEN;
import static com.systematic.trading.simulation.brokerage.BrokerageFeeUtil.TEN_BASIS_POINTS;
import static com.systematic.trading.simulation.brokerage.BrokerageFeeUtil.THIRTEEN;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.MathContext;

import org.junit.Test;

import com.systematic.trading.simulation.brokerage.EquityClass;
import com.systematic.trading.simulation.brokerage.fees.BellDirectFeeStructure;
import com.systematic.trading.simulation.exception.UnsupportedEquityClass;

/**
 * Test the fee structure for Bell Direct.
 * 
 * @author CJ Hare
 */
public class BellDirectFeeStructureTest {
	private final MathContext mc = MathContext.DECIMAL64;

	@Test
	public void firstTradeFlatFee() {
		final int tradesThisMonth = 1;
		final BigDecimal tradeValue = BigDecimal.valueOf( 1000 );
		final BellDirectFeeStructure feeStructure = new BellDirectFeeStructure( mc );

		final BigDecimal fee = feeStructure.calculateFee( tradeValue, EquityClass.STOCK, tradesThisMonth );

		assertEquals( FIFTEEN, fee );
	}

	@Test
	public void tenthTradeFlatFee() {
		final int tradesThisMonth = 10;
		final BigDecimal tradeValue = BigDecimal.valueOf( 1000 );
		final BellDirectFeeStructure feeStructure = new BellDirectFeeStructure( mc );

		final BigDecimal fee = feeStructure.calculateFee( tradeValue, EquityClass.STOCK, tradesThisMonth );

		assertEquals( FIFTEEN, fee );
	}

	@Test
	public void eleventhTradeFlatFee() {
		final int tradesThisMonth = 11;
		final BigDecimal tradeValue = BigDecimal.valueOf( 1000 );
		final BellDirectFeeStructure feeStructure = new BellDirectFeeStructure( mc );

		final BigDecimal fee = feeStructure.calculateFee( tradeValue, EquityClass.STOCK, tradesThisMonth );

		assertEquals( THIRTEEN, fee );
	}

	@Test
	public void thirteithTradeFlatFee() {
		final int tradesThisMonth = 30;
		final BigDecimal tradeValue = BigDecimal.valueOf( 1000 );
		final BellDirectFeeStructure feeStructure = new BellDirectFeeStructure( mc );

		final BigDecimal fee = feeStructure.calculateFee( tradeValue, EquityClass.STOCK, tradesThisMonth );

		assertEquals( THIRTEEN, fee );
	}

	@Test
	public void thirtyFirstTradeFlatFee() {
		final int tradesThisMonth = 31;
		final BigDecimal tradeValue = BigDecimal.valueOf( 1000 );
		final BellDirectFeeStructure feeStructure = new BellDirectFeeStructure( mc );

		final BigDecimal fee = feeStructure.calculateFee( tradeValue, EquityClass.STOCK, tradesThisMonth );

		assertEquals( TEN, fee );
	}

	@Test
	public void firstTradePercentageFee() {
		final int tradesThisMonth = 1;
		final BigDecimal tradeValue = BigDecimal.valueOf( 50000 );
		final BigDecimal expectedFee = tradeValue.multiply( TEN_BASIS_POINTS, mc );
		final BellDirectFeeStructure feeStructure = new BellDirectFeeStructure( mc );

		final BigDecimal fee = feeStructure.calculateFee( tradeValue, EquityClass.STOCK, tradesThisMonth );

		assertEquals( expectedFee, fee );
	}

	@Test
	public void tenthTradePercentageFee() {
		final int tradesThisMonth = 10;
		final BigDecimal tradeValue = BigDecimal.valueOf( 50000 );
		final BigDecimal expectedFee = tradeValue.multiply( TEN_BASIS_POINTS, mc );
		final BellDirectFeeStructure feeStructure = new BellDirectFeeStructure( mc );

		final BigDecimal fee = feeStructure.calculateFee( tradeValue, EquityClass.STOCK, tradesThisMonth );

		assertEquals( expectedFee, fee );
	}

	@Test
	public void eleventhTradePercentageFee() {
		final int tradesThisMonth = 11;
		final BigDecimal tradeValue = BigDecimal.valueOf( 50000 );
		final BigDecimal expectedFee = tradeValue.multiply( EIGHT_BASIS_POINTS, mc );
		final BellDirectFeeStructure feeStructure = new BellDirectFeeStructure( mc );

		final BigDecimal fee = feeStructure.calculateFee( tradeValue, EquityClass.STOCK, tradesThisMonth );

		assertEquals( expectedFee, fee );
	}

	@Test
	public void thirteithTradePercentageFee() {
		final int tradesThisMonth = 30;
		final BigDecimal tradeValue = BigDecimal.valueOf( 50000 );
		final BigDecimal expectedFee = tradeValue.multiply( EIGHT_BASIS_POINTS, mc );
		final BellDirectFeeStructure feeStructure = new BellDirectFeeStructure( mc );

		final BigDecimal fee = feeStructure.calculateFee( tradeValue, EquityClass.STOCK, tradesThisMonth );

		assertEquals( expectedFee, fee );
	}

	@Test
	public void thirtyFirstTradePercentageFee() {
		final int tradesThisMonth = 31;
		final BigDecimal tradeValue = BigDecimal.valueOf( 50000 );
		final BigDecimal expectedFee = tradeValue.multiply( EIGHT_BASIS_POINTS, mc );
		final BellDirectFeeStructure feeStructure = new BellDirectFeeStructure( mc );

		final BigDecimal fee = feeStructure.calculateFee( tradeValue, EquityClass.STOCK, tradesThisMonth );

		assertEquals( expectedFee, fee );
	}

	@Test(expected = UnsupportedEquityClass.class)
	public void equityClassFuture() {
		final BellDirectFeeStructure feeStructure = new BellDirectFeeStructure( mc );
		feeStructure.calculateFee( BigDecimal.ZERO, EquityClass.FUTURE, 0 );
	}

	@Test(expected = UnsupportedEquityClass.class)
	public void equityClassForex() {
		final BellDirectFeeStructure feeStructure = new BellDirectFeeStructure( mc );
		feeStructure.calculateFee( BigDecimal.ZERO, EquityClass.FOREX, 0 );
	}

	@Test(expected = UnsupportedEquityClass.class)
	public void equityClassMetal() {
		final BellDirectFeeStructure feeStructure = new BellDirectFeeStructure( mc );
		feeStructure.calculateFee( BigDecimal.ZERO, EquityClass.METAL, 0 );
	}
}
