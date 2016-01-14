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
package com.systematic.trading.backtest.brokerage;

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

import com.systematic.trading.data.price.Price;
import com.systematic.trading.model.EquityClass;
import com.systematic.trading.model.EquityIdentity;
import com.systematic.trading.simulation.brokerage.SingleEquityClassBroker;
import com.systematic.trading.simulation.brokerage.fee.BrokerageTransactionFeeStructure;
import com.systematic.trading.simulation.equity.fee.EquityManagementFeeStructure;
import com.systematic.trading.simulation.order.EquityOrderVolume;
import com.systematic.trading.simulation.order.exception.InsufficientEquitiesException;

/**
 * Single Equity Class Broker test.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class SingleEquityClassBrokerTest {
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	@Mock
	private BrokerageTransactionFeeStructure fees;

	@Mock
	private EquityManagementFeeStructure equityFee;

	@Mock
	private EquityIdentity equity;

	@Test
	public void getEquityBalance() {
		final SingleEquityClassBroker broker = new SingleEquityClassBroker( fees, equityFee, equity, LocalDate.now(),
				MATH_CONTEXT );

		final BigDecimal balance = broker.getEquityBalance();

		assertEquals( BigDecimal.ZERO, balance );
	}

	@Test
	public void buy() {
		final Price price = Price.valueOf( BigDecimal.valueOf( 101 ) );
		final BigDecimal equityVolume = BigDecimal.valueOf( 11 );
		final EquityOrderVolume volume = EquityOrderVolume.valueOf( equityVolume );
		final SingleEquityClassBroker broker = new SingleEquityClassBroker( fees, equityFee, equity, LocalDate.now(),
				MATH_CONTEXT );
		final BigDecimal transactionCost = BigDecimal.valueOf( 10.99 );
		when( fees.calculateFee( any( BigDecimal.class ), any( EquityClass.class ), anyInt() ) )
				.thenReturn( transactionCost );
		final LocalDate date = LocalDate.now();

		broker.buy( price, volume, date );

		assertEquals( 11, broker.getEquityBalance().intValue() );
	}

	@Test
	public void calculateBuy() {
		final Price price = Price.valueOf( BigDecimal.valueOf( 101 ) );
		final BigDecimal equityVolume = BigDecimal.valueOf( 11 );
		final EquityOrderVolume volume = EquityOrderVolume.valueOf( equityVolume );
		final SingleEquityClassBroker broker = new SingleEquityClassBroker( fees, equityFee, equity, LocalDate.now(),
				MATH_CONTEXT );
		final BigDecimal transactionCost = BigDecimal.valueOf( 10.99 );
		when( fees.calculateFee( any( BigDecimal.class ), any( EquityClass.class ), anyInt() ) )
				.thenReturn( transactionCost );
		final LocalDate date = LocalDate.now();

		final BigDecimal cost = broker.calculateBuy( price, volume, date );

		final BigDecimal tradeValue = price.getPrice().multiply( volume.getVolume(), MATH_CONTEXT );
		assertEquals( tradeValue.add( transactionCost, MATH_CONTEXT ), cost );
		assertEquals( 0, broker.getEquityBalance().intValue() );
	}

	@Test
	/**
	 * Buying from a non-zero balance
	 */
	public void buyAdditional() {
		final Price price = Price.valueOf( BigDecimal.valueOf( 101 ) );
		final BigDecimal equityVolume = BigDecimal.valueOf( 11 );
		final EquityOrderVolume volume = EquityOrderVolume.valueOf( equityVolume );
		final SingleEquityClassBroker broker = new SingleEquityClassBroker( fees, equityFee, equity, LocalDate.now(),
				MATH_CONTEXT );
		final BigDecimal transactionCost = BigDecimal.valueOf( 10.99 );
		when( fees.calculateFee( any( BigDecimal.class ), any( EquityClass.class ), anyInt() ) )
				.thenReturn( transactionCost );
		final LocalDate date = LocalDate.now();
		final BigDecimal startingBalance = BigDecimal.valueOf( 22.5 );
		broker.buy( price, EquityOrderVolume.valueOf( startingBalance ), date );

		broker.buy( price, volume, date );

		assertEquals( 33, broker.getEquityBalance().intValue() );
	}

	@Test
	/**
	 * Buying from a non-zero balance
	 */
	public void caculateBuyAdditional() {
		final Price price = Price.valueOf( BigDecimal.valueOf( 101 ) );
		final BigDecimal equityVolume = BigDecimal.valueOf( 11 );
		final EquityOrderVolume volume = EquityOrderVolume.valueOf( equityVolume );
		final SingleEquityClassBroker broker = new SingleEquityClassBroker( fees, equityFee, equity, LocalDate.now(),
				MATH_CONTEXT );
		final BigDecimal transactionCost = BigDecimal.valueOf( 10.99 );
		when( fees.calculateFee( any( BigDecimal.class ), any( EquityClass.class ), anyInt() ) )
				.thenReturn( transactionCost );
		final LocalDate date = LocalDate.now();
		final BigDecimal startingBalance = BigDecimal.valueOf( 22.5 );
		broker.buy( price, EquityOrderVolume.valueOf( startingBalance ), date );

		final BigDecimal cost = broker.calculateBuy( price, volume, date );

		final BigDecimal tradeValue = price.getPrice().multiply( volume.getVolume(), MATH_CONTEXT );
		assertEquals( tradeValue.add( transactionCost, MATH_CONTEXT ), cost );
		assertEquals( 22, broker.getEquityBalance().intValue() );
	}

	@Test
	public void sell() throws InsufficientEquitiesException {
		final Price price = Price.valueOf( BigDecimal.valueOf( 101 ) );
		final BigDecimal equityVolume = BigDecimal.valueOf( 11 );
		final EquityOrderVolume volume = EquityOrderVolume.valueOf( equityVolume );
		final SingleEquityClassBroker broker = new SingleEquityClassBroker( fees, equityFee, equity, LocalDate.now(),
				MATH_CONTEXT );
		final BigDecimal transactionCost = BigDecimal.valueOf( 10.99 );
		when( fees.calculateFee( any( BigDecimal.class ), any( EquityClass.class ), anyInt() ) )
				.thenReturn( transactionCost );
		final LocalDate date = LocalDate.now();
		final BigDecimal startingBalance = BigDecimal.valueOf( 18 );
		broker.buy( price, EquityOrderVolume.valueOf( startingBalance ), date );

		final BigDecimal cost = broker.sell( price, volume, date );

		final BigDecimal tradeValue = price.getPrice().multiply( volume.getVolume(), MATH_CONTEXT );
		assertEquals( tradeValue.subtract( transactionCost, MATH_CONTEXT ), cost );
		assertEquals( 7, broker.getEquityBalance().intValue() );
	}

	@Test(expected = InsufficientEquitiesException.class)
	public void sellWithException() throws InsufficientEquitiesException {
		final Price price = Price.valueOf( BigDecimal.valueOf( 101 ) );
		final EquityOrderVolume volume = EquityOrderVolume.valueOf( BigDecimal.valueOf( 12 ) );
		final SingleEquityClassBroker broker = new SingleEquityClassBroker( fees, equityFee, equity, LocalDate.now(),
				MATH_CONTEXT );

		broker.sell( price, volume, LocalDate.now() );
	}

	@Test
	public void calculateFee() {
		final BigDecimal transactionCost = BigDecimal.valueOf( 6.78787 );
		final BigDecimal tradeValue = BigDecimal.valueOf( 101 );
		when( fees.calculateFee( any( BigDecimal.class ), any( EquityClass.class ), anyInt() ) )
				.thenReturn( transactionCost );

		final SingleEquityClassBroker broker = new SingleEquityClassBroker( fees, equityFee, equity, LocalDate.now(),
				MATH_CONTEXT );

		final BigDecimal fees = broker.calculateFee( tradeValue, EquityClass.STOCK, LocalDate.now() );

		assertEquals( transactionCost, fees );
	}
}
