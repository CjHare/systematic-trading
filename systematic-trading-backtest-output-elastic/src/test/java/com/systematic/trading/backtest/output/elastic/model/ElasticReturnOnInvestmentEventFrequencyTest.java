/**
 * Copyright (c) 2015-2018, CJ Hare All rights reserved.
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
package com.systematic.trading.backtest.output.elastic.model;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.simulation.analysis.roi.event.ReturnOnInvestmentEvent;

/**
 * Verifying that ElasticReturnOnInvestmentEventFrequency produces the correct mappings.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class ElasticReturnOnInvestmentEventFrequencyTest {

	@Mock
	private ReturnOnInvestmentEvent event;

	@Test
	public void yearly() {

		setUpEvent(LocalDate.of(2008, 02, 12), LocalDate.of(2009, 02, 12));

		final String frequency = eventFrequency();

		assertEquals(ElasticTypeValue.YEARLY, frequency);
	}

	@Test
	public void justOverYearly() {

		setUpEvent(LocalDate.of(2010, 02, 12), LocalDate.of(2011, 02, 14));

		final String frequency = eventFrequency();

		assertEquals(ElasticTypeValue.YEARLY, frequency);
	}

	@Test
	public void justUnderYearly() {

		setUpEvent(LocalDate.of(2011, 02, 14), LocalDate.of(2012, 02, 13));

		final String frequency = eventFrequency();

		assertEquals(ElasticTypeValue.YEARLY, frequency);
	}

	@Test
	public void monthly() {

		setUpEvent(LocalDate.of(2008, 01, 12), LocalDate.of(2008, 02, 12));

		final String frequency = eventFrequency();

		assertEquals(ElasticTypeValue.MONTHLY, frequency);
	}

	@Test
	public void justOverMonthly() {

		setUpEvent(LocalDate.of(2010, 01, 12), LocalDate.of(2010, 02, 14));

		final String frequency = eventFrequency();

		assertEquals(ElasticTypeValue.MONTHLY, frequency);
	}

	@Test
	public void justUnderMonthly() {

		setUpEvent(LocalDate.of(2011, 01, 14), LocalDate.of(2011, 02, 13));

		final String frequency = eventFrequency();

		assertEquals(ElasticTypeValue.MONTHLY, frequency);
	}

	@Test
	public void weekly() {

		setUpEvent(LocalDate.of(2008, 01, 12), LocalDate.of(2008, 01, 19));

		final String frequency = eventFrequency();

		assertEquals(ElasticTypeValue.WEEKLY, frequency);
	}

	@Test
	public void justOverWeekly() {

		setUpEvent(LocalDate.of(2010, 01, 12), LocalDate.of(2010, 01, 20));

		final String frequency = eventFrequency();

		assertEquals(ElasticTypeValue.WEEKLY, frequency);
	}

	@Test
	public void justUnderWeekly() {

		setUpEvent(LocalDate.of(2011, 01, 14), LocalDate.of(2011, 01, 19));

		final String frequency = eventFrequency();

		assertEquals(ElasticTypeValue.WEEKLY, frequency);
	}

	@Test
	public void daily() {

		setUpEvent(LocalDate.of(2008, 01, 12), LocalDate.of(2008, 01, 12));

		final String frequency = eventFrequency();

		assertEquals(ElasticTypeValue.DAILY, frequency);
	}

	@Test
	public void justAboveDaily() {

		setUpEvent(LocalDate.of(2008, 01, 12), LocalDate.of(2008, 01, 16));

		final String frequency = eventFrequency();

		assertEquals(ElasticTypeValue.DAILY, frequency);
	}

	private String eventFrequency() {

		return new ElasticReturnOnInvestmentEventFrequency(event).frequency();
	}

	private void setUpEvent( final LocalDate startDateInclusive, final LocalDate endDateExclusive ) {

		when(event.startDateInclusive()).thenReturn(startDateInclusive);
		when(event.endDateExclusive()).thenReturn(endDateExclusive);
	}
}
