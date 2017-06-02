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
package com.systematic.trading.backtest.analysis.roi;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Period;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.price.ClosingPrice;
import com.systematic.trading.simulation.analysis.roi.CulmativeReturnOnInvestmentCalculator;
import com.systematic.trading.simulation.analysis.roi.ReturnOnInvestmentCalculator;
import com.systematic.trading.simulation.analysis.roi.event.ReturnOnInvestmentEvent;
import com.systematic.trading.simulation.analysis.roi.event.ReturnOnInvestmentEventListener;
import com.systematic.trading.simulation.brokerage.Brokerage;
import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.simulation.cash.event.CashAccountEvent;
import com.systematic.trading.simulation.cash.event.CashEvent.CashEventType;

/**
 * Tests the CulmativeReturnOnInvestmentCalculator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class CulmativeReturnOnInvestmentCalculatorTest {

	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	private static final LocalDate TODAY = LocalDate.now();
	private static final LocalDate YESTERFAY = TODAY.minus(Period.ofDays(1));
	private static final LocalDate TWO_DAYS_AGO = TODAY.minus(Period.ofDays(2));

	private static final BigDecimal NO_CHANGE = BigDecimal.ZERO;
	private static final BigDecimal TEN_PERCENT_CHANGE = BigDecimal.TEN;
	private static final BigDecimal TEN_POINT_ONE_PERCENT_CHANGE = BigDecimal.valueOf(10.1);

	@Mock
	private Brokerage broker;

	@Mock
	private CashAccount cashAccount;

	@Mock
	private TradingDayPrices tradingData;

	@Mock
	private ReturnOnInvestmentEventListener listener;

	@Test
	public void firstUpdateNoDeposit() {
		setUpCashBalance(4.5);
		setUpEquityBalance(11.22);
		setUpTradingData(99.87);
		final ReturnOnInvestmentCalculator calculator = setUpReturnOnInvestmentCalculator();

		// Process and generate notifications
		calculator.update(broker, cashAccount, tradingData);

		verify(listener).event(isExpectedRoiEvent(NO_CHANGE, YESTERFAY, TODAY));
	}

	@Test
	public void firstUpdateWithOneDeposit() {
		setUpCashBalance(4.5);
		setUpEquityBalance(11.22);
		setUpTradingData(99.87);
		final ReturnOnInvestmentCalculator calculator = setUpReturnOnInvestmentCalculator();

		// Deposit the amount currently in the cash balance
		calculator.event(new CashAccountEvent(BigDecimal.valueOf(0), BigDecimal.valueOf(4.5), BigDecimal.valueOf(4.5),
		        CashEventType.DEPOSIT, TODAY));

		// Process and generate notifications
		calculator.update(broker, cashAccount, tradingData);

		verify(listener).event(isExpectedRoiEvent(NO_CHANGE, YESTERFAY, TODAY));
	}

	@Test
	public void firstUpdateWithThreeeDeposits() {
		setUpCashBalance(4.5);
		setUpEquityBalance(11.22);
		setUpTradingData(99.87);
		final ReturnOnInvestmentCalculator calculator = setUpReturnOnInvestmentCalculator();

		// Deposit the amount currently in the cash balance
		calculator.event(new CashAccountEvent(BigDecimal.valueOf(0), BigDecimal.valueOf(1.5), BigDecimal.valueOf(1.5),
		        CashEventType.DEPOSIT, TODAY));
		calculator.event(new CashAccountEvent(BigDecimal.valueOf(1.5), BigDecimal.valueOf(3.0), BigDecimal.valueOf(1.5),
		        CashEventType.DEPOSIT, TODAY));
		calculator.event(new CashAccountEvent(BigDecimal.valueOf(3.0), BigDecimal.valueOf(4.5), BigDecimal.valueOf(1.5),
		        CashEventType.DEPOSIT, TODAY));

		// Process and generate notifications
		calculator.update(broker, cashAccount, tradingData);

		verify(listener).event(isExpectedRoiEvent(NO_CHANGE, YESTERFAY, TODAY));
	}

	@Test
	public void twoUpdatesNoDeposit() {
		setUpCashBalance(0);
		setUpEquityBalance(10, 11);
		setUpTradingData(99.87, 99.87);
		final ReturnOnInvestmentCalculator calculator = setUpReturnOnInvestmentCalculator();

		// Process and generate notifications
		calculator.update(broker, cashAccount, tradingData);
		calculator.update(broker, cashAccount, tradingData);

		//TODO inOrder
		verify(listener).event(isExpectedRoiEvent(NO_CHANGE, TWO_DAYS_AGO, YESTERFAY));
		verify(listener).event(isExpectedRoiEvent(TEN_PERCENT_CHANGE, YESTERFAY, TODAY));
	}

	@Test
	public void twoUpdatesOneDeposit() {
		setUpCashBalance(0, 866);
		setUpEquityBalance(10, 11);
		setUpTradingData(99.87, 99.87);
		final ReturnOnInvestmentCalculator calculator = setUpReturnOnInvestmentCalculator();

		// Process and generate notifications
		calculator.update(broker, cashAccount, tradingData);

		// Deposit the amount currently in the cash balance
		calculator.event(new CashAccountEvent(BigDecimal.valueOf(0), BigDecimal.valueOf(866), BigDecimal.valueOf(866),
		        CashEventType.DEPOSIT, TODAY));

		calculator.update(broker, cashAccount, tradingData);

		//TODO inOrder
		verify(listener).event(isExpectedRoiEvent(NO_CHANGE, TWO_DAYS_AGO, YESTERFAY));
		verify(listener).event(isExpectedRoiEvent(TEN_PERCENT_CHANGE, YESTERFAY, TODAY));
	}

	@Test
	public void twoUpdatesThreeDeposits() {
		setUpCashBalance(0, 866);
		setUpEquityBalance(10, 11);
		setUpTradingData(99.87, 99.87);
		final ReturnOnInvestmentCalculator calculator = setUpReturnOnInvestmentCalculator();

		// Process and generate notifications
		calculator.update(broker, cashAccount, tradingData);

		// Deposit the amount currently in the cash balance
		calculator.event(new CashAccountEvent(BigDecimal.valueOf(0), BigDecimal.valueOf(500), BigDecimal.valueOf(500),
		        CashEventType.DEPOSIT, TODAY));
		calculator.event(new CashAccountEvent(BigDecimal.valueOf(500), BigDecimal.valueOf(766), BigDecimal.valueOf(266),
		        CashEventType.DEPOSIT, TODAY));
		calculator.event(new CashAccountEvent(BigDecimal.valueOf(766), BigDecimal.valueOf(866), BigDecimal.valueOf(100),
		        CashEventType.DEPOSIT, TODAY));

		calculator.update(broker, cashAccount, tradingData);

		//TODO inOrder
		verify(listener).event(isExpectedRoiEvent(NO_CHANGE, TWO_DAYS_AGO, YESTERFAY));
		verify(listener).event(isExpectedRoiEvent(TEN_PERCENT_CHANGE, YESTERFAY, TODAY));
	}

	@Test
	public void twoUpdatesOneDepositPlusInterest() {
		setUpCashBalance(0, 867);
		setUpEquityBalance(10, 11);
		setUpTradingData(100, 100);
		final ReturnOnInvestmentCalculator calculator = setUpReturnOnInvestmentCalculator();

		// Process and generate notifications
		calculator.update(broker, cashAccount, tradingData);

		// Deposit the amount currently in the cash balance
		calculator.event(new CashAccountEvent(BigDecimal.valueOf(0), BigDecimal.valueOf(866), BigDecimal.valueOf(866),
		        CashEventType.DEPOSIT, TODAY));

		// Interest is counted as an increase in ROI
		calculator.event(new CashAccountEvent(BigDecimal.valueOf(0), BigDecimal.valueOf(866), BigDecimal.valueOf(1),
		        CashEventType.INTEREST, TODAY));

		calculator.update(broker, cashAccount, tradingData);

		//TODO inOrder
		verify(listener).event(isExpectedRoiEvent(NO_CHANGE, TWO_DAYS_AGO, YESTERFAY));
		verify(listener).event(isExpectedRoiEvent(TEN_POINT_ONE_PERCENT_CHANGE, YESTERFAY, TODAY));
	}

	private ReturnOnInvestmentCalculator setUpReturnOnInvestmentCalculator() {
		final ReturnOnInvestmentCalculator calculator = new CulmativeReturnOnInvestmentCalculator(MATH_CONTEXT);
		calculator.addListener(listener);
		return calculator;
	}

	private void setUpEquityBalance( final double... balances ) {
		OngoingStubbing<BigDecimal> getEquityBalance = when(broker.getEquityBalance());

		for (final double balance : balances) {
			getEquityBalance = getEquityBalance.thenReturn(BigDecimal.valueOf(balance));
		}
	}

	private void setUpTradingData( final double... closingPrices ) {
		setUpTradingDataClosingPrice(closingPrices);
		setUpTradingDataDate(closingPrices.length);
	}

	private void setUpTradingDataClosingPrice( final double... closingPrices ) {
		OngoingStubbing<ClosingPrice> getClosingPrice = when(tradingData.getClosingPrice());

		for (final double closingPrice : closingPrices) {
			getClosingPrice = getClosingPrice.thenReturn(ClosingPrice.valueOf(BigDecimal.valueOf(closingPrice)));
		}
	}

	private void setUpCashBalance( final double... cashBalances ) {
		OngoingStubbing<BigDecimal> getBalance = when(cashAccount.getBalance());

		for (final double cashBalance : cashBalances) {
			getBalance = getBalance.thenReturn(BigDecimal.valueOf(cashBalance));
		}
	}

	/**
	 * Trading data entries are sorted with oldest first.
	 */
	private void setUpTradingDataDate( final int days ) {
		OngoingStubbing<LocalDate> getData = when(tradingData.getDate());

		for (int i = days - 1; i >= 0; i--) {
			getData = getData.thenReturn(TODAY.minus(Period.ofDays(i)));
		}
	}

	private ReturnOnInvestmentEvent isExpectedRoiEvent( final BigDecimal percentageChange,
	        final LocalDate startDateInclusive, final LocalDate endDateInclusive ) {
		return argThat(new RoiEventMatcher(percentageChange, startDateInclusive, endDateInclusive));
	}
}