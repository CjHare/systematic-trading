/**
 * Copyright (c) 2015-2017-2017, CJ Hare All rights reserved.
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

import org.hamcrest.Description;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
		final BigDecimal equityBalance = BigDecimal.valueOf(11.22);
		when(broker.getEquityBalance()).thenReturn(equityBalance);
		final ClosingPrice closingPrice = ClosingPrice.valueOf(BigDecimal.valueOf(99.87));
		when(tradingData.getClosingPrice()).thenReturn(closingPrice);
		final BigDecimal cashBalance = BigDecimal.valueOf(4.5);
		when(cashAccount.getBalance()).thenReturn(cashBalance);
		final LocalDate now = LocalDate.now();
		when(tradingData.getDate()).thenReturn(now);

		final ReturnOnInvestmentCalculator calculator = new CulmativeReturnOnInvestmentCalculator(MATH_CONTEXT);
		calculator.addListener(listener);

		// Process and generate notifications
		calculator.update(broker, cashAccount, tradingData);

		// Verify the event is as expected
		final BigDecimal expectedPercentageChange = BigDecimal.valueOf(0);
		final LocalDate expectedStartDate = now.minus(Period.ofDays(1));

		verify(listener).event(isExpectedRoiEvent(expectedPercentageChange, expectedStartDate, now));
	}

	@Test
	public void firstUpdateWithOneDeposit() {
		final BigDecimal equityBalance = BigDecimal.valueOf(11.22);
		when(broker.getEquityBalance()).thenReturn(equityBalance);
		final ClosingPrice closingPrice = ClosingPrice.valueOf(BigDecimal.valueOf(99.87));
		when(tradingData.getClosingPrice()).thenReturn(closingPrice);
		final BigDecimal cashBalance = BigDecimal.valueOf(4.5);
		when(cashAccount.getBalance()).thenReturn(cashBalance);
		final LocalDate now = LocalDate.now();
		when(tradingData.getDate()).thenReturn(now);

		final ReturnOnInvestmentCalculator calculator = new CulmativeReturnOnInvestmentCalculator(MATH_CONTEXT);
		calculator.addListener(listener);

		// Deposit the amount currently in the cash balance
		calculator.event(new CashAccountEvent(BigDecimal.valueOf(0), BigDecimal.valueOf(4.5), BigDecimal.valueOf(4.5),
		        CashEventType.DEPOSIT, now));

		// Process and generate notifications
		calculator.update(broker, cashAccount, tradingData);

		// Verify the event is as expected
		final BigDecimal expectedPercentageChange = BigDecimal.valueOf(0);
		final LocalDate expectedStartDate = now.minus(Period.ofDays(1));

		verify(listener).event(isExpectedRoiEvent(expectedPercentageChange, expectedStartDate, now));
	}

	@Test
	public void firstUpdateWithThreeeDeposits() {
		final BigDecimal equityBalance = BigDecimal.valueOf(11.22);
		when(broker.getEquityBalance()).thenReturn(equityBalance);
		final ClosingPrice closingPrice = ClosingPrice.valueOf(BigDecimal.valueOf(99.87));
		when(tradingData.getClosingPrice()).thenReturn(closingPrice);
		final BigDecimal cashBalance = BigDecimal.valueOf(4.5);
		when(cashAccount.getBalance()).thenReturn(cashBalance);
		final LocalDate now = LocalDate.now();
		when(tradingData.getDate()).thenReturn(now);

		final ReturnOnInvestmentCalculator calculator = new CulmativeReturnOnInvestmentCalculator(MATH_CONTEXT);
		calculator.addListener(listener);

		// Deposit the amount currently in the cash balance
		calculator.event(new CashAccountEvent(BigDecimal.valueOf(0), BigDecimal.valueOf(1.5), BigDecimal.valueOf(1.5),
		        CashEventType.DEPOSIT, now));
		calculator.event(new CashAccountEvent(BigDecimal.valueOf(1.5), BigDecimal.valueOf(3.0), BigDecimal.valueOf(1.5),
		        CashEventType.DEPOSIT, now));
		calculator.event(new CashAccountEvent(BigDecimal.valueOf(3.0), BigDecimal.valueOf(4.5), BigDecimal.valueOf(1.5),
		        CashEventType.DEPOSIT, now));

		// Process and generate notifications
		calculator.update(broker, cashAccount, tradingData);

		// Verify the event is as expected
		final BigDecimal expectedPercentageChange = BigDecimal.valueOf(0);
		final LocalDate expectedStartDate = now.minus(Period.ofDays(1));

		verify(listener).event(isExpectedRoiEvent(expectedPercentageChange, expectedStartDate, now));
	}

	@Test
	public void twoUpdatesNoDeposit() {
		final BigDecimal equityBalanceYesterday = BigDecimal.valueOf(10);
		final BigDecimal equityBalanceToday = BigDecimal.valueOf(11);
		when(broker.getEquityBalance()).thenReturn(equityBalanceYesterday).thenReturn(equityBalanceToday);
		final ClosingPrice closingPriceYesterday = ClosingPrice.valueOf(BigDecimal.valueOf(99.87));
		final ClosingPrice closingPriceToday = ClosingPrice.valueOf(BigDecimal.valueOf(99.87));
		when(tradingData.getClosingPrice()).thenReturn(closingPriceYesterday).thenReturn(closingPriceToday);
		final BigDecimal cashBalance = BigDecimal.valueOf(0);
		when(cashAccount.getBalance()).thenReturn(cashBalance);
		final LocalDate yesterday = LocalDate.now();
		final LocalDate today = LocalDate.now();
		when(tradingData.getDate()).thenReturn(yesterday).thenReturn(today);

		final ReturnOnInvestmentCalculator calculator = new CulmativeReturnOnInvestmentCalculator(MATH_CONTEXT);
		calculator.addListener(listener);

		// Process and generate notifications
		calculator.update(broker, cashAccount, tradingData);
		calculator.update(broker, cashAccount, tradingData);

		// Verify the event is as expected
		final BigDecimal expectedPercentageChangeYesterday = BigDecimal.valueOf(0);
		final BigDecimal expectedPercentageChangeToday = BigDecimal.valueOf(10);
		final LocalDate expectedStartDate = yesterday.minus(Period.ofDays(1));

		verify(listener).event(isExpectedRoiEvent(expectedPercentageChangeYesterday, expectedStartDate, yesterday));
		verify(listener).event(isExpectedRoiEvent(expectedPercentageChangeToday, yesterday, today));
	}

	@Test
	public void twoUpdatesOneDeposit() {
		final BigDecimal equityBalanceYesterday = BigDecimal.valueOf(10);
		final BigDecimal equityBalanceToday = BigDecimal.valueOf(11);
		when(broker.getEquityBalance()).thenReturn(equityBalanceYesterday).thenReturn(equityBalanceToday);
		final ClosingPrice closingPriceYesterday = ClosingPrice.valueOf(BigDecimal.valueOf(99.87));
		final ClosingPrice closingPriceToday = ClosingPrice.valueOf(BigDecimal.valueOf(99.87));
		when(tradingData.getClosingPrice()).thenReturn(closingPriceYesterday).thenReturn(closingPriceToday);
		final BigDecimal cashBalanceYesterday = BigDecimal.valueOf(0);
		final BigDecimal cashBalanceToday = BigDecimal.valueOf(866);
		when(cashAccount.getBalance()).thenReturn(cashBalanceYesterday).thenReturn(cashBalanceToday);
		final LocalDate yesterday = LocalDate.now();
		final LocalDate today = LocalDate.now();
		when(tradingData.getDate()).thenReturn(yesterday).thenReturn(today);

		final ReturnOnInvestmentCalculator calculator = new CulmativeReturnOnInvestmentCalculator(MATH_CONTEXT);
		calculator.addListener(listener);

		// Process and generate notifications
		calculator.update(broker, cashAccount, tradingData);

		// Deposit the amount currently in the cash balance
		calculator.event(new CashAccountEvent(BigDecimal.valueOf(0), BigDecimal.valueOf(866), BigDecimal.valueOf(866),
		        CashEventType.DEPOSIT, today));

		calculator.update(broker, cashAccount, tradingData);

		// Verify the event is as expected
		final BigDecimal expectedPercentageChangeYesterday = BigDecimal.valueOf(0);
		final BigDecimal expectedPercentageChangeToday = BigDecimal.valueOf(10);
		final LocalDate expectedStartDate = yesterday.minus(Period.ofDays(1));

		verify(listener).event(isExpectedRoiEvent(expectedPercentageChangeYesterday, expectedStartDate, yesterday));
		verify(listener).event(isExpectedRoiEvent(expectedPercentageChangeToday, yesterday, today));
	}

	@Test
	public void twoUpdatesThreeDeposit() {
		final BigDecimal equityBalanceYesterday = BigDecimal.valueOf(10);
		final BigDecimal equityBalanceToday = BigDecimal.valueOf(11);
		when(broker.getEquityBalance()).thenReturn(equityBalanceYesterday).thenReturn(equityBalanceToday);
		final ClosingPrice closingPriceYesterday = ClosingPrice.valueOf(BigDecimal.valueOf(99.87));
		final ClosingPrice closingPriceToday = ClosingPrice.valueOf(BigDecimal.valueOf(99.87));
		when(tradingData.getClosingPrice()).thenReturn(closingPriceYesterday).thenReturn(closingPriceToday);
		final BigDecimal cashBalanceYesterday = BigDecimal.valueOf(0);
		final BigDecimal cashBalanceToday = BigDecimal.valueOf(866);
		when(cashAccount.getBalance()).thenReturn(cashBalanceYesterday).thenReturn(cashBalanceToday);
		final LocalDate yesterday = LocalDate.now();
		final LocalDate today = LocalDate.now();
		when(tradingData.getDate()).thenReturn(yesterday).thenReturn(today);

		final ReturnOnInvestmentCalculator calculator = new CulmativeReturnOnInvestmentCalculator(MATH_CONTEXT);
		calculator.addListener(listener);

		// Process and generate notifications
		calculator.update(broker, cashAccount, tradingData);

		// Deposit the amount currently in the cash balance
		calculator.event(new CashAccountEvent(BigDecimal.valueOf(0), BigDecimal.valueOf(500), BigDecimal.valueOf(500),
		        CashEventType.DEPOSIT, today));
		calculator.event(new CashAccountEvent(BigDecimal.valueOf(500), BigDecimal.valueOf(766), BigDecimal.valueOf(266),
		        CashEventType.DEPOSIT, today));
		calculator.event(new CashAccountEvent(BigDecimal.valueOf(766), BigDecimal.valueOf(866), BigDecimal.valueOf(100),
		        CashEventType.DEPOSIT, today));

		calculator.update(broker, cashAccount, tradingData);

		// Verify the event is as expected
		final BigDecimal expectedPercentageChangeYesterday = BigDecimal.valueOf(0);
		final BigDecimal expectedPercentageChangeToday = BigDecimal.valueOf(10);
		final LocalDate expectedStartDate = yesterday.minus(Period.ofDays(1));

		verify(listener).event(isExpectedRoiEvent(expectedPercentageChangeYesterday, expectedStartDate, yesterday));
		verify(listener).event(isExpectedRoiEvent(expectedPercentageChangeToday, yesterday, today));
	}

	@Test
	public void twoUpdatesOneDepositPlusInterest() {
		final BigDecimal equityBalanceYesterday = BigDecimal.valueOf(10);
		final BigDecimal equityBalanceToday = BigDecimal.valueOf(11);
		when(broker.getEquityBalance()).thenReturn(equityBalanceYesterday).thenReturn(equityBalanceToday);
		final ClosingPrice closingPriceYesterday = ClosingPrice.valueOf(BigDecimal.valueOf(100));
		final ClosingPrice closingPriceToday = ClosingPrice.valueOf(BigDecimal.valueOf(100));
		when(tradingData.getClosingPrice()).thenReturn(closingPriceYesterday).thenReturn(closingPriceToday);
		final BigDecimal cashBalanceYesterday = BigDecimal.valueOf(0);
		final BigDecimal cashBalanceToday = BigDecimal.valueOf(867);
		when(cashAccount.getBalance()).thenReturn(cashBalanceYesterday).thenReturn(cashBalanceToday);
		final LocalDate yesterday = LocalDate.now();
		final LocalDate today = LocalDate.now();
		when(tradingData.getDate()).thenReturn(yesterday).thenReturn(today);

		final ReturnOnInvestmentCalculator calculator = new CulmativeReturnOnInvestmentCalculator(MATH_CONTEXT);
		calculator.addListener(listener);

		// Process and generate notifications
		calculator.update(broker, cashAccount, tradingData);

		// Deposit the amount currently in the cash balance
		calculator.event(new CashAccountEvent(BigDecimal.valueOf(0), BigDecimal.valueOf(866), BigDecimal.valueOf(866),
		        CashEventType.DEPOSIT, today));

		// Interest is counted as an increase in ROI
		calculator.event(new CashAccountEvent(BigDecimal.valueOf(0), BigDecimal.valueOf(866), BigDecimal.valueOf(1),
		        CashEventType.INTEREST, today));

		calculator.update(broker, cashAccount, tradingData);

		// Verify the event is as expected
		final BigDecimal expectedPercentageChangeYesterday = BigDecimal.valueOf(0);
		final BigDecimal expectedPercentageChangeToday = BigDecimal.valueOf(10.1);
		final LocalDate expectedStartDate = yesterday.minus(Period.ofDays(1));

		verify(listener).event(isExpectedRoiEvent(expectedPercentageChangeYesterday, expectedStartDate, yesterday));
		verify(listener).event(isExpectedRoiEvent(expectedPercentageChangeToday, yesterday, today));
	}

	private ReturnOnInvestmentEvent isExpectedRoiEvent( final BigDecimal percentageChange,
	        final LocalDate startDateInclusive, final LocalDate endDateInclusive ) {
		return argThat(new RoiEventMatcher(percentageChange, startDateInclusive, endDateInclusive));
	}

	class RoiEventMatcher extends ArgumentMatcher<ReturnOnInvestmentEvent> {
		private final BigDecimal percentageChange;
		private final LocalDate startDateExclusive;
		private final LocalDate endDateInclusive;

		RoiEventMatcher(final BigDecimal percentageChange, final LocalDate startDateExclusive,
		        final LocalDate endDateInclusive) {
			this.percentageChange = percentageChange;
			this.startDateExclusive = startDateExclusive;
			this.endDateInclusive = endDateInclusive;
		}

		@Override
		public boolean matches( final Object argument ) {
			final ReturnOnInvestmentEvent event = (ReturnOnInvestmentEvent) argument;

			return percentageChange.compareTo(event.getPercentageChange()) == 0
			        && startDateExclusive.equals(event.getExclusiveStartDate())
			        && endDateInclusive.equals(event.getInclusiveEndDate());
		}

		@Override
		public void describeTo( Description description ) {
			description
			        .appendText(String.format("Percentage change: %s, Exclusive start date: %s, Inclusive end date: %s",
			                percentageChange, startDateExclusive, endDateInclusive));
		}
	}
}
