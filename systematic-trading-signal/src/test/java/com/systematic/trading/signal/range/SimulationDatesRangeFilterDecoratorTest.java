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
package com.systematic.trading.signal.range;

import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.time.Month.MARCH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.model.price.TradingDayPrices;

/**
 * SimulationDatesRangeFilterDecorator decorates a date filter adding a earliest and latest date
 * behavior.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class SimulationDatesRangeFilterDecoratorTest {

	@Mock
	private SignalRangeFilter filter;

	/** Decorator instance being tested. */
	private SimulationDatesRangeFilterDecorator decorator;

	@Test
	public void beforeEarliestDate() {

		final TradingDayPrices[] priceData = new TradingDayPrices[1];
		final LocalDate simulationStartDate = LocalDate.of(2000, FEBRUARY, 1);
		final LocalDate simulationEndDate = LocalDate.of(2010, MARCH, 1);
		final LocalDate priceDataStartDate = LocalDate.of(2000, JANUARY, 15);
		setUpFilterEarliestDate(priceDataStartDate, priceData);
		setUpDecorate(simulationStartDate, simulationEndDate);

		final LocalDate earliestDate = earliestSignalDate(priceData);

		verifyDate(simulationStartDate, earliestDate);
		verifyEarliestDateDelegation(priceData);
	}

	@Test
	public void onEarliestDate() {

		final TradingDayPrices[] priceData = new TradingDayPrices[2];
		final LocalDate simulationStartDate = LocalDate.of(2001, FEBRUARY, 1);
		final LocalDate simulationEndDate = LocalDate.of(2011, MARCH, 1);
		final LocalDate priceDataStartDate = simulationStartDate;
		setUpFilterEarliestDate(priceDataStartDate, priceData);
		setUpDecorate(simulationStartDate, simulationEndDate);

		final LocalDate earliestDate = earliestSignalDate(priceData);

		verifyDate(simulationStartDate, earliestDate);
		verifyEarliestDateDelegation(priceData);
	}

	@Test
	public void afterEarliestDate() {

		final TradingDayPrices[] priceData = new TradingDayPrices[3];
		final LocalDate simulationStartDate = LocalDate.of(2002, FEBRUARY, 1);
		final LocalDate simulationEndDate = LocalDate.of(2012, MARCH, 1);
		final LocalDate priceDataStartDate = LocalDate.of(2002, FEBRUARY, 15);
		setUpFilterEarliestDate(priceDataStartDate, priceData);
		setUpDecorate(simulationStartDate, simulationEndDate);

		final LocalDate earliestDate = earliestSignalDate(priceData);

		verifyDate(priceDataStartDate, earliestDate);
		verifyEarliestDateDelegation(priceData);
	}

	@Test
	public void beforeLatesttDate() {

		final TradingDayPrices[] priceData = new TradingDayPrices[4];
		final LocalDate simulationStartDate = LocalDate.of(2000, MARCH, 1);
		final LocalDate simulationEndDate = LocalDate.of(2010, FEBRUARY, 1);
		final LocalDate priceDataEndDate = LocalDate.of(2010, JANUARY, 15);
		setUpFilterLatestDate(priceDataEndDate, priceData);
		setUpDecorate(simulationStartDate, simulationEndDate);

		final LocalDate latestDate = latestSignalDate(priceData);

		verifyDate(priceDataEndDate, latestDate);
		verifyLatestDateDelegation(priceData);
	}

	@Test
	public void onLatesttDate() {

		final TradingDayPrices[] priceData = new TradingDayPrices[5];
		final LocalDate simulationStartDate = LocalDate.of(2003, MARCH, 1);
		final LocalDate simulationEndDate = LocalDate.of(2013, FEBRUARY, 1);
		final LocalDate priceDataEndDate = LocalDate.of(2013, FEBRUARY, 1);
		setUpFilterLatestDate(priceDataEndDate, priceData);
		setUpDecorate(simulationStartDate, simulationEndDate);

		final LocalDate latestDate = latestSignalDate(priceData);

		verifyDate(priceDataEndDate, latestDate);
		verifyLatestDateDelegation(priceData);
	}

	@Test
	public void afterLatesttDate() {

		final TradingDayPrices[] priceData = new TradingDayPrices[6];
		final LocalDate simulationStartDate = LocalDate.of(2005, MARCH, 1);
		final LocalDate simulationEndDate = LocalDate.of(2015, FEBRUARY, 1);
		final LocalDate priceDataEndDate = LocalDate.of(2015, FEBRUARY, 22);
		setUpFilterLatestDate(priceDataEndDate, priceData);
		setUpDecorate(simulationStartDate, simulationEndDate);

		final LocalDate latestDate = latestSignalDate(priceData);

		verifyDate(simulationEndDate, latestDate);
		verifyLatestDateDelegation(priceData);
	}

	private LocalDate earliestSignalDate( final TradingDayPrices[] priceData ) {

		return decorator.earliestSignalDate(priceData);
	}

	private LocalDate latestSignalDate( final TradingDayPrices[] priceData ) {

		return decorator.latestSignalDate(priceData);
	}

	private void setUpDecorate( final LocalDate simulationStartDate, final LocalDate simulationEndDate ) {

		decorator = new SimulationDatesRangeFilterDecorator(simulationStartDate, simulationEndDate, filter);
	}

	private void setUpFilterEarliestDate( final LocalDate earliestDate, final TradingDayPrices[] data ) {

		when(filter.earliestSignalDate(any(TradingDayPrices[].class))).thenReturn(earliestDate);
	}

	private void setUpFilterLatestDate( final LocalDate latestDate, final TradingDayPrices[] data ) {

		when(filter.latestSignalDate(any(TradingDayPrices[].class))).thenReturn(latestDate);
	}

	private void verifyDate( final LocalDate expected, final LocalDate actual ) {

		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	private void verifyEarliestDateDelegation( final TradingDayPrices[] data ) {

		verify(filter).earliestSignalDate(data);
	}

	private void verifyLatestDateDelegation( final TradingDayPrices[] data ) {

		verify(filter).latestSignalDate(data);
	}
}