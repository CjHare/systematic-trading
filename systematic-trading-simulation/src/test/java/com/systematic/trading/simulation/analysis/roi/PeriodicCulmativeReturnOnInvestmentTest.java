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
package com.systematic.trading.simulation.analysis.roi;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.simulation.analysis.roi.event.ReturnOnInvestmentEvent;
import com.systematic.trading.simulation.analysis.roi.event.ReturnOnInvestmentEventListener;
import com.systematic.trading.simulation.matcher.RoiEventMatcher;

/**
 * Tests the PeriodicCulmativeReturnOnInvestment.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class PeriodicCulmativeReturnOnInvestmentTest {

	private static final Period UNDER_SUMMARY_PERIOD = Period.ofDays(1);
	private static final Period SUMMARY_PERIOD = Period.ofDays(2);
	private static final Period OVER_SUMMARY_PERIOD = Period.ofDays(3);

	private static final LocalDate STARTING_DATE = LocalDate.now();
	private static final LocalDate UNDER_END_DATE = STARTING_DATE.plus(UNDER_SUMMARY_PERIOD);
	private static final LocalDate END_DATE = STARTING_DATE.plus(SUMMARY_PERIOD);
	private static final LocalDate OVER_END_DATE = STARTING_DATE.plus(OVER_SUMMARY_PERIOD);

	@Mock
	private ReturnOnInvestmentEventListener listener;

	/** The ROI calculator being unit tested.*/
	private PeriodicCulmativeReturnOnInvestment calculator;

	@Before
	public void setUp() {

		calculator = new PeriodicCulmativeReturnOnInvestment(STARTING_DATE, SUMMARY_PERIOD);
		calculator.addListener(listener);
	}

	@Test
	public void underTimePeriod() {

		final ReturnOnInvestmentEvent event = setUpRoiEvent(199.33, UNDER_END_DATE);

		event(event);

		verifyEventInteraction(event);
		verifyNoCulumativeRoiEvent();
	}

	@Test
	public void onTimePeriod() {

		final ReturnOnInvestmentEvent event = setUpRoiEvent(199.33, END_DATE);

		event(event);

		verifyEventInteraction(event);
		verifyCulumativeRoiEvent(199.33, STARTING_DATE, END_DATE);
	}

	@Test
	public void afterTimePeriod() {

		final ReturnOnInvestmentEvent event = setUpRoiEvent(199.33, OVER_END_DATE);

		event(event);

		verifyEventInteraction(event);
		verifyCulumativeRoiEvent(199.33, STARTING_DATE, OVER_END_DATE);
	}

	@Test
	public void onTimePeriodTwoEvents() {

		final ReturnOnInvestmentEvent eventOne = setUpRoiEvent(19.12, UNDER_END_DATE);
		final ReturnOnInvestmentEvent eventTwo = setUpRoiEvent(25.37, END_DATE);

		event(eventOne);
		event(eventTwo);

		verifyEventInteraction(eventOne);
		verifyEventInteraction(eventTwo);
		verifyCulumativeRoiEvent(44.49, STARTING_DATE, END_DATE);
	}

	@Test
	public void afterTimePeriodTwoEvents() {

		final ReturnOnInvestmentEvent eventOne = setUpRoiEvent(19.12, UNDER_END_DATE);
		final ReturnOnInvestmentEvent eventTwo = setUpRoiEvent(25.37, OVER_END_DATE);

		event(eventOne);
		event(eventTwo);

		verifyEventInteraction(eventOne);
		verifyEventInteraction(eventTwo);
		verifyCulumativeRoiEvent(44.49, STARTING_DATE, OVER_END_DATE);
	}

	private void verifyCulumativeRoiEvent( final double percentageChange, final LocalDate startDateInclusive,
	        final LocalDate endDateInclusive ) {

		verify(listener).event(isExpectedRoiEvent(percentageChange, startDateInclusive, endDateInclusive));
		verifyNoMoreInteractions(listener);
	}

	private void verifyNoCulumativeRoiEvent() {

		verifyZeroInteractions(listener);
	}

	private void verifyEventInteraction( final ReturnOnInvestmentEvent event ) {

		verify(event).percentageChange();
		verify(event).exclusiveEndDate();
	}

	private void event( final ReturnOnInvestmentEvent event ) {

		calculator.event(event);
	}

	private ReturnOnInvestmentEvent isExpectedRoiEvent( final double percentageChange,
	        final LocalDate startDateInclusive, final LocalDate endDateInclusive ) {

		return RoiEventMatcher.argumentMatches(BigDecimal.valueOf(percentageChange), startDateInclusive,
		        endDateInclusive);
	}

	private ReturnOnInvestmentEvent setUpRoiEvent( final double change, final LocalDate endDate ) {

		final ReturnOnInvestmentEvent event = mock(ReturnOnInvestmentEvent.class);
		when(event.percentageChange()).thenReturn(BigDecimal.valueOf(change));
		when(event.exclusiveEndDate()).thenReturn(endDate);
		return event;
	}
}