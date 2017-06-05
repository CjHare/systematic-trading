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
package com.systematic.trading.backtest.brokerage;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;

import org.junit.Before;
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
	private static final BigDecimal EQUITY_PRICE = BigDecimal.valueOf(101);

	@Mock
	private BrokerageTransactionFeeStructure fees;

	@Mock
	private EquityManagementFeeStructure equityFee;

	@Mock
	private EquityIdentity equity;

	private SingleEquityClassBroker broker;

	@Before
	public void setUp() {
		broker = new SingleEquityClassBroker("BrokerName", fees, equityFee, equity, LocalDate.now(),
		        MathContext.DECIMAL64);
	}

	@Test
	public void buy() {
		setUpBrokerageFee(10.99);

		buy(11);

		verifyEquityBalance(11);
	}

	@Test
	public void calculateBuy() {
		setUpBrokerageFee(1.23);

		final BigDecimal cost = calculateBuy(11);

		verifyCost(1112.23, cost);
		verifyEquityBalance(0);
	}

	@Test
	/**
	 * Buying from a non-zero balance
	 */
	public void buyAdditional() {
		setUpBrokerageFee(7.89);

		buy(22.5);
		buy(11);

		verifyEquityBalance(33.5);
	}

	@Test
	/**
	 * Calculating the buy cost does not increment held equities.
	 */
	public void caculateBuyAdditional() {
		setUpBrokerageFee(10.99);
		buy(22.5);

		final BigDecimal cost = calculateBuy(11);

		verifyCost(1121.99, cost);
		verifyEquityBalance(22.5);
	}

	@Test
	public void sell() throws InsufficientEquitiesException {
		setUpBrokerageFee(10.78);
		buy(18);

		final BigDecimal cost = sell(11);

		verifyCost(1100.22, cost);
		verifyEquityBalance(7);
	}

	@Test(expected = InsufficientEquitiesException.class)
	public void sellWithException() throws InsufficientEquitiesException {
		sell(1);
	}

	@Test
	public void calculateFee() {
		setUpBrokerageFee(6.78787);

		final BigDecimal fee = broker.calculateFee(EQUITY_PRICE, EquityClass.STOCK, LocalDate.now());

		verifyCost(6.78787, fee);
	}

	//TODO verify calculateFee call is made

	private BigDecimal sell( final double volume ) throws InsufficientEquitiesException {
		return broker.sell(Price.valueOf(EQUITY_PRICE), EquityOrderVolume.valueOf(BigDecimal.valueOf(volume)),
		        LocalDate.now());
	}

	private BigDecimal calculateBuy( final double volume ) {
		return broker.calculateBuy(Price.valueOf(EQUITY_PRICE), EquityOrderVolume.valueOf(BigDecimal.valueOf(volume)),
		        LocalDate.now());
	}

	private void buy( final double equityVolume ) {
		broker.buy(Price.valueOf(EQUITY_PRICE), EquityOrderVolume.valueOf(BigDecimal.valueOf(equityVolume)),
		        LocalDate.now());
	}

	private void verifyCost( final double expected, final BigDecimal cost ) {
		assertEquals(String.format("Expected %s != Cost %s", expected, cost),
		        BigDecimal.valueOf(expected).compareTo(cost), 0);
	}

	private void verifyEquityBalance( final double expected ) {
		assertEquals(String.format("Expected %s != Equity Balance %s", expected, broker.getEquityBalance()),
		        BigDecimal.valueOf(expected).compareTo(broker.getEquityBalance()), 0);
	}

	private void setUpBrokerageFee( final double transactionCost ) {
		when(fees.calculateFee(any(BigDecimal.class), any(EquityClass.class), anyInt()))
		        .thenReturn(BigDecimal.valueOf(transactionCost));
	}
}