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
package com.systematic.trading.backtest.analysis.roi;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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

import com.systematic.trading.simulation.analysis.roi.PeriodicCulmativeReturnOnInvestmentCalculator;
import com.systematic.trading.simulation.analysis.roi.event.ReturnOnInvestmentEvent;
import com.systematic.trading.simulation.analysis.roi.event.ReturnOnInvestmentEventListener;

/**
 * Tests the PeriodicCulmativeReturnOnInvestmentCalculator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class PeriodicCulmativeReturnOnInvestmentCalculatorTest {
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	@Mock
	private ReturnOnInvestmentEventListener listener;

	@Test
	public void underTimePeriod() {
		final Period summaryPeriod = Period.ofDays( 2 );
		final LocalDate startingDate = LocalDate.now();

		final PeriodicCulmativeReturnOnInvestmentCalculator calculator = new PeriodicCulmativeReturnOnInvestmentCalculator(
				startingDate, summaryPeriod, MATH_CONTEXT );
		calculator.addListener( listener );

		final ReturnOnInvestmentEvent event = mock( ReturnOnInvestmentEvent.class );
		final BigDecimal expectedChange = BigDecimal.valueOf( 199.33 );
		when( event.getPercentageChange() ).thenReturn( expectedChange );
		when( event.getInclusiveEndDate() ).thenReturn( startingDate.plus( Period.ofDays( 1 ) ) );

		calculator.event( event );

		verify( event ).getPercentageChange();
		verify( event ).getInclusiveEndDate();
		verifyNoMoreInteractions( listener );
	}

	@Test
	public void onTimePeriod() {
		final Period summaryPeriod = Period.ofDays( 2 );
		final LocalDate startingDate = LocalDate.now();

		final PeriodicCulmativeReturnOnInvestmentCalculator calculator = new PeriodicCulmativeReturnOnInvestmentCalculator(
				startingDate, summaryPeriod, MATH_CONTEXT );
		calculator.addListener( listener );

		final ReturnOnInvestmentEvent event = mock( ReturnOnInvestmentEvent.class );
		final BigDecimal expectedChange = BigDecimal.valueOf( 199.33 );
		when( event.getPercentageChange() ).thenReturn( expectedChange );
		final LocalDate expectedEndDate = startingDate.plus( Period.ofDays( 2 ) );
		when( event.getInclusiveEndDate() ).thenReturn( expectedEndDate );

		calculator.event( event );

		verify( event ).getPercentageChange();
		verify( event ).getInclusiveEndDate();
		verify( listener ).event( isExpectedRoiEvent( expectedChange, startingDate, expectedEndDate ) );
		verifyNoMoreInteractions( listener );
	}

	@Test
	public void afterTimePeriod() {
		final Period summaryPeriod = Period.ofDays( 2 );
		final LocalDate startingDate = LocalDate.now();

		final PeriodicCulmativeReturnOnInvestmentCalculator calculator = new PeriodicCulmativeReturnOnInvestmentCalculator(
				startingDate, summaryPeriod, MATH_CONTEXT );
		calculator.addListener( listener );

		final ReturnOnInvestmentEvent event = mock( ReturnOnInvestmentEvent.class );
		final BigDecimal expectedChange = BigDecimal.valueOf( 199.33 );
		when( event.getPercentageChange() ).thenReturn( expectedChange );
		final LocalDate expectedEndDate = startingDate.plus( Period.ofDays( 3 ) );
		when( event.getInclusiveEndDate() ).thenReturn( expectedEndDate );

		calculator.event( event );

		verify( event ).getPercentageChange();
		verify( event ).getInclusiveEndDate();
		verify( listener ).event( isExpectedRoiEvent( expectedChange, startingDate, expectedEndDate ) );
		verifyNoMoreInteractions( listener );
	}

	@Test
	public void onTimePeriodTwoEvents() {
		final Period summaryPeriod = Period.ofDays( 2 );
		final LocalDate startingDate = LocalDate.now();

		final PeriodicCulmativeReturnOnInvestmentCalculator calculator = new PeriodicCulmativeReturnOnInvestmentCalculator(
				startingDate, summaryPeriod, MATH_CONTEXT );
		calculator.addListener( listener );

		final ReturnOnInvestmentEvent eventOne = mock( ReturnOnInvestmentEvent.class );
		final BigDecimal firstChange = BigDecimal.valueOf( 19.12 );
		when( eventOne.getPercentageChange() ).thenReturn( firstChange );
		final LocalDate firstEndDate = startingDate.plus( Period.ofDays( 1 ) );
		when( eventOne.getInclusiveEndDate() ).thenReturn( firstEndDate );

		final ReturnOnInvestmentEvent eventTwo = mock( ReturnOnInvestmentEvent.class );
		final BigDecimal secondChange = BigDecimal.valueOf( 25.37 );
		when( eventTwo.getPercentageChange() ).thenReturn( secondChange );
		final LocalDate secondEndDate = firstEndDate.plus( Period.ofDays( 1 ) );
		when( eventTwo.getInclusiveEndDate() ).thenReturn( secondEndDate );

		calculator.event( eventOne );
		calculator.event( eventTwo );

		verify( eventOne ).getPercentageChange();
		verify( eventOne ).getInclusiveEndDate();
		verify( eventTwo ).getPercentageChange();
		verify( eventTwo ).getInclusiveEndDate();

		final BigDecimal expectedChange = firstChange.add( secondChange, MATH_CONTEXT );
		verify( listener )
				.event( isExpectedRoiEvent( expectedChange, startingDate, startingDate.plus( Period.ofDays( 2 ) ) ) );
		verifyNoMoreInteractions( listener );
	}

	@Test
	public void afterTimePeriodTwoEvents() {
		final Period summaryPeriod = Period.ofDays( 2 );
		final LocalDate startingDate = LocalDate.now();

		final PeriodicCulmativeReturnOnInvestmentCalculator calculator = new PeriodicCulmativeReturnOnInvestmentCalculator(
				startingDate, summaryPeriod, MATH_CONTEXT );
		calculator.addListener( listener );

		final ReturnOnInvestmentEvent eventOne = mock( ReturnOnInvestmentEvent.class );
		final BigDecimal firstChange = BigDecimal.valueOf( 19.12 );
		when( eventOne.getPercentageChange() ).thenReturn( firstChange );
		final LocalDate firstEndDate = startingDate.plus( Period.ofDays( 1 ) );
		when( eventOne.getInclusiveEndDate() ).thenReturn( firstEndDate );

		final ReturnOnInvestmentEvent eventTwo = mock( ReturnOnInvestmentEvent.class );
		final BigDecimal secondChange = BigDecimal.valueOf( 25.37 );
		when( eventTwo.getPercentageChange() ).thenReturn( secondChange );
		final LocalDate secondEndDate = firstEndDate.plus( Period.ofDays( 1 ) );
		when( eventTwo.getInclusiveEndDate() ).thenReturn( secondEndDate );

		calculator.event( eventOne );
		calculator.event( eventTwo );

		verify( eventOne ).getPercentageChange();
		verify( eventOne ).getInclusiveEndDate();
		verify( eventTwo ).getPercentageChange();
		verify( eventTwo ).getInclusiveEndDate();

		final BigDecimal expectedChange = firstChange.add( secondChange, MATH_CONTEXT );
		verify( listener )
				.event( isExpectedRoiEvent( expectedChange, startingDate, startingDate.plus( Period.ofDays( 2 ) ) ) );
		verifyNoMoreInteractions( listener );
	}

	private ReturnOnInvestmentEvent isExpectedRoiEvent( final BigDecimal percentageChange,
			final LocalDate startDateInclusive, final LocalDate endDateInclusive ) {
		return argThat( new RoiEventMatcher( percentageChange, startDateInclusive, endDateInclusive ) );
	}

	class RoiEventMatcher extends ArgumentMatcher<ReturnOnInvestmentEvent> {
		private final BigDecimal percentageChange;
		private final LocalDate startDateExclusive;
		private final LocalDate endDateInclusive;

		RoiEventMatcher( final BigDecimal percentageChange, final LocalDate startDateExclusive,
				final LocalDate endDateInclusive ) {
			this.percentageChange = percentageChange;
			this.startDateExclusive = startDateExclusive;
			this.endDateInclusive = endDateInclusive;
		}

		@Override
		public boolean matches( final Object argument ) {
			final ReturnOnInvestmentEvent event = (ReturnOnInvestmentEvent) argument;

			return percentageChange.compareTo( event.getPercentageChange() ) == 0
					&& startDateExclusive.equals( event.getExclusiveStartDate() )
					&& endDateInclusive.equals( event.getInclusiveEndDate() );
		}

		@Override
		public void describeTo( Description description ) {
			description.appendText(
					String.format( "Percentage change: %s, Exclusive start date: %s, Inclusive end date: %s",
							percentageChange, startDateExclusive, endDateInclusive ) );
		}
	}
}
