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
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.time.LocalDate;

import org.hamcrest.Description;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.brokerage.EquityClass;
import com.systematic.trading.backtest.brokerage.fees.BrokerageFeeStructure;
import com.systematic.trading.backtest.event.impl.BrokerageAccountEvent;
import com.systematic.trading.backtest.event.impl.BrokerageAccountEvent.BrokerageAccountEventType;
import com.systematic.trading.backtest.exception.InsufficientEquitiesException;
import com.systematic.trading.backtest.order.EquityOrderVolume;
import com.systematic.trading.data.price.Price;
import com.systematic.trading.event.recorder.EventRecorder;

/**
 * Single Equity Class Broker test.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class SingleEquityClassBrokerTest {
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;
	private static final DecimalFormat TWO_DECIMAL_PLACES;

	static {
		TWO_DECIMAL_PLACES = new DecimalFormat();
		TWO_DECIMAL_PLACES.setMaximumFractionDigits( 2 );
		TWO_DECIMAL_PLACES.setMinimumFractionDigits( 2 );
		TWO_DECIMAL_PLACES.setGroupingUsed( false );
	}

	@Mock
	private BrokerageFeeStructure fees;

	@Mock
	private EventRecorder recorder;

	@Test
	public void getEquityBalance() {
		final SingleEquityClassBroker broker = new SingleEquityClassBroker( fees, EquityClass.STOCK, recorder,
				MATH_CONTEXT );

		final BigDecimal balance = broker.getEquityBalance();

		assertEquals( BigDecimal.ZERO, balance );
	}

	@Test
	public void buy() {
		final Price price = Price.valueOf( BigDecimal.valueOf( 101 ) );
		final BigDecimal equityVolume = BigDecimal.valueOf( 11 );
		final EquityOrderVolume volume = EquityOrderVolume.valueOf( equityVolume );
		final SingleEquityClassBroker broker = new SingleEquityClassBroker( fees, EquityClass.STOCK, recorder,
				MATH_CONTEXT );
		final BigDecimal transactionCost = BigDecimal.valueOf( 10.99 );
		when( fees.calculateFee( any( BigDecimal.class ), any( EquityClass.class ), anyInt() ) ).thenReturn(
				transactionCost );
		final LocalDate date = LocalDate.now();

		final BigDecimal cost = broker.buy( price, volume, date );

		final BigDecimal tradeValue = price.getPrice().multiply( volume.getVolume(), MATH_CONTEXT );
		assertEquals( tradeValue.add( transactionCost, MATH_CONTEXT ), cost );
		assertEquals( 11, broker.getEquityBalance().intValue() );

		verify( recorder ).record(
				isBrokerageAccountEvent( BigDecimal.ZERO, equityVolume, equityVolume, BrokerageAccountEventType.BUY,
						date ) );
	}

	@Test
	/**
	 * Buying from a non-zero balance
	 */
	public void buyAdditional() {
		final Price price = Price.valueOf( BigDecimal.valueOf( 101 ) );
		final BigDecimal equityVolume = BigDecimal.valueOf( 11 );
		final EquityOrderVolume volume = EquityOrderVolume.valueOf( equityVolume );
		final SingleEquityClassBroker broker = new SingleEquityClassBroker( fees, EquityClass.STOCK, recorder,
				MATH_CONTEXT );
		final BigDecimal transactionCost = BigDecimal.valueOf( 10.99 );
		when( fees.calculateFee( any( BigDecimal.class ), any( EquityClass.class ), anyInt() ) ).thenReturn(
				transactionCost );
		final LocalDate date = LocalDate.now();
		final BigDecimal startingBalance = BigDecimal.valueOf( 22.5 );
		broker.buy( price, EquityOrderVolume.valueOf( startingBalance ), date );

		final BigDecimal cost = broker.buy( price, volume, date );

		final BigDecimal tradeValue = price.getPrice().multiply( volume.getVolume(), MATH_CONTEXT );
		assertEquals( tradeValue.add( transactionCost, MATH_CONTEXT ), cost );
		assertEquals( 33, broker.getEquityBalance().intValue() );

		verify( recorder ).record(
				isBrokerageAccountEvent( startingBalance, startingBalance.add( equityVolume, MATH_CONTEXT ),
						equityVolume, BrokerageAccountEventType.BUY, date ) );
	}

	@Test
	public void sell() throws InsufficientEquitiesException {
		final Price price = Price.valueOf( BigDecimal.valueOf( 101 ) );
		final BigDecimal equityVolume = BigDecimal.valueOf( 11 );
		final EquityOrderVolume volume = EquityOrderVolume.valueOf( equityVolume );
		final SingleEquityClassBroker broker = new SingleEquityClassBroker( fees, EquityClass.STOCK, recorder,
				MATH_CONTEXT );
		final BigDecimal transactionCost = BigDecimal.valueOf( 10.99 );
		when( fees.calculateFee( any( BigDecimal.class ), any( EquityClass.class ), anyInt() ) ).thenReturn(
				transactionCost );
		final LocalDate date = LocalDate.now();
		final BigDecimal startingBalance = BigDecimal.valueOf( 18 );
		broker.buy( price, EquityOrderVolume.valueOf( startingBalance ), date );

		final BigDecimal cost = broker.sell( price, volume, date );

		final BigDecimal tradeValue = price.getPrice().multiply( volume.getVolume(), MATH_CONTEXT );
		assertEquals( tradeValue.subtract( transactionCost, MATH_CONTEXT ), cost );
		assertEquals( 7, broker.getEquityBalance().intValue() );
		verify( recorder ).record(
				isBrokerageAccountEvent( BigDecimal.ZERO, startingBalance, startingBalance,
						BrokerageAccountEventType.BUY, date ) );
		verify( recorder ).record(
				isBrokerageAccountEvent( startingBalance, startingBalance.subtract( equityVolume, MATH_CONTEXT ),
						equityVolume, BrokerageAccountEventType.SELL, date ) );
	}

	@Test(expected = InsufficientEquitiesException.class)
	public void sellWithException() throws InsufficientEquitiesException {
		final Price price = Price.valueOf( BigDecimal.valueOf( 101 ) );
		final EquityOrderVolume volume = EquityOrderVolume.valueOf( BigDecimal.valueOf( 12 ) );
		final SingleEquityClassBroker broker = new SingleEquityClassBroker( fees, EquityClass.STOCK, recorder,
				MATH_CONTEXT );

		broker.sell( price, volume, LocalDate.now() );
	}

	@Test
	public void calculateFee() {
		final BigDecimal transactionCost = BigDecimal.valueOf( 6.78787 );
		final BigDecimal tradeValue = BigDecimal.valueOf( 101 );
		when( fees.calculateFee( any( BigDecimal.class ), any( EquityClass.class ), anyInt() ) ).thenReturn(
				transactionCost );

		final SingleEquityClassBroker broker = new SingleEquityClassBroker( fees, EquityClass.STOCK, recorder,
				MATH_CONTEXT );

		final BigDecimal fees = broker.calculateFee( tradeValue, EquityClass.STOCK, LocalDate.now() );

		assertEquals( transactionCost, fees );
	}

	private BrokerageAccountEvent isBrokerageAccountEvent( final BigDecimal fundsBefore, final BigDecimal fundsAfter,
			final BigDecimal volume, final BrokerageAccountEventType type, final LocalDate transactionDate ) {
		return argThat( new IsBrokerageAccountEventArgument( fundsBefore, fundsAfter, volume, type, transactionDate ) );
	}

	class IsBrokerageAccountEventArgument extends ArgumentMatcher<BrokerageAccountEvent> {

		private final String volume;
		private final String balanceBefore;
		private final String balanceAfter;
		private final LocalDate transactionDate;
		private final BrokerageAccountEventType type;

		public IsBrokerageAccountEventArgument( final BigDecimal balanceBefore, final BigDecimal balanceAfter,
				final BigDecimal volume, final BrokerageAccountEventType type, final LocalDate transactionDate ) {
			this.balanceBefore = TWO_DECIMAL_PLACES.format( balanceBefore );
			this.balanceAfter = TWO_DECIMAL_PLACES.format( balanceAfter );
			this.volume = TWO_DECIMAL_PLACES.format( volume );
			this.transactionDate = transactionDate;
			this.type = type;
		}

		@Override
		public boolean matches( final Object argument ) {

			if (argument instanceof BrokerageAccountEvent) {
				final BrokerageAccountEvent event = (BrokerageAccountEvent) argument;
				return volume.equals( event.getAmount() ) && balanceBefore.equals( event.getFundsBefore() )
						&& balanceAfter.equals( event.getFundsAfter() )
						&& transactionDate.equals( event.getTransactionDate() ) && type == event.getType();
			}

			return false;
		}

		@Override
		public void describeTo( final Description description ) {
			description.appendText( String.format( "Before: %s, After: %s, Volume: %s, Type: %s, Date: %s",
					balanceBefore, balanceAfter, volume, type, transactionDate ) );
		}
	}
}
