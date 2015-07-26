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
package com.systematic.trading.backtest.brokerage.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.brokerage.EquityClass;
import com.systematic.trading.backtest.brokerage.fees.BrokerageFeeStructure;
import com.systematic.trading.backtest.exception.InsufficientEquitiesException;
import com.systematic.trading.backtest.order.EquityOrderVolume;
import com.systematic.trading.data.price.Price;

/**
 * Single Equity Class Broker test.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class SingleEquityClassBrokerTest {

	private final MathContext context = MathContext.DECIMAL64;

	@Mock
	private BrokerageFeeStructure fees;

	@Test
	public void getEquityBalance() {
		final SingleEquityClassBroker broker = new SingleEquityClassBroker( fees, EquityClass.STOCK, context );

		final BigDecimal balance = broker.getEquityBalance();

		assertEquals( BigDecimal.ZERO, balance );
	}

	@Test
	public void buy() {
		final Price price = Price.valueOf( BigDecimal.valueOf( 101 ) );
		final EquityOrderVolume volume = EquityOrderVolume.valueOf( BigDecimal.valueOf( 11 ) );
		final SingleEquityClassBroker broker = new SingleEquityClassBroker( fees, EquityClass.STOCK, context );
		final BigDecimal transactionCost = BigDecimal.valueOf( 10.99 );
		when( fees.calculateFee( any( BigDecimal.class ), any( EquityClass.class ), anyInt() ) ).thenReturn(
				transactionCost );

		final BigDecimal cost = broker.buy( price, volume, LocalDate.now() );

		final BigDecimal tradeValue = price.getPrice().multiply( volume.getVolume(), context );
		assertEquals( tradeValue.add( transactionCost, context ), cost );
		assertEquals( 11, broker.getEquityBalance().intValue() );
	}

	@Test
	public void sell() throws InsufficientEquitiesException {
		final Price price = Price.valueOf( BigDecimal.valueOf( 101 ) );
		final EquityOrderVolume volume = EquityOrderVolume.valueOf( BigDecimal.valueOf( 12 ) );
		final SingleEquityClassBroker broker = new SingleEquityClassBroker( fees, EquityClass.STOCK, context );
		final BigDecimal transactionCost = BigDecimal.valueOf( 10.99 );
		when( fees.calculateFee( any( BigDecimal.class ), any( EquityClass.class ), anyInt() ) ).thenReturn(
				transactionCost );

		broker.buy( price, EquityOrderVolume.valueOf( BigDecimal.valueOf( 18 ) ), LocalDate.now() );

		final BigDecimal cost = broker.sell( price, volume, LocalDate.now() );

		final BigDecimal tradeValue = price.getPrice().multiply( volume.getVolume(), context );
		assertEquals( tradeValue.subtract( transactionCost, context ), cost );
		assertEquals( 6, broker.getEquityBalance().intValue() );
	}

	@Test(expected = InsufficientEquitiesException.class)
	public void sellWithException() throws InsufficientEquitiesException {
		final Price price = Price.valueOf( BigDecimal.valueOf( 101 ) );
		final EquityOrderVolume volume = EquityOrderVolume.valueOf( BigDecimal.valueOf( 12 ) );
		final SingleEquityClassBroker broker = new SingleEquityClassBroker( fees, EquityClass.STOCK, context );

		broker.sell( price, volume, LocalDate.now() );
	}

	@Test
	public void calculateFee() {
		final BigDecimal transactionCost = BigDecimal.valueOf( 6.78787 );
		final BigDecimal tradeValue = BigDecimal.valueOf( 101 );
		when( fees.calculateFee( any( BigDecimal.class ), any( EquityClass.class ), anyInt() ) ).thenReturn(
				transactionCost );

		final SingleEquityClassBroker broker = new SingleEquityClassBroker( fees, EquityClass.STOCK, context );

		final BigDecimal fees = broker.calculateFee( tradeValue, EquityClass.STOCK, LocalDate.now() );

		assertEquals( transactionCost, fees );
	}
}