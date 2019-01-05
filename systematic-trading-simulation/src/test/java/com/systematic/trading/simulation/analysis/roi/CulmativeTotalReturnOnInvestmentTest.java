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
package com.systematic.trading.simulation.analysis.roi;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.simulation.analysis.roi.event.ReturnOnInvestmentEvent;

/**
 * Tests the behaviour of the CulmativeTotalReturnOnInvestment.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class CulmativeTotalReturnOnInvestmentTest {

	private CulmativeTotalReturnOnInvestment calculator;

	@Before
	public void setUp() {

		calculator = new CulmativeTotalReturnOnInvestment();
	}

	@Test
	public void noEvents() {

		verfiyCulumateRoi(0);
	}

	@Test
	public void oneEvent() {

		event(101);

		verfiyCulumateRoi(101);
	}

	@Test
	public void threeEvents() {

		event(22);
		event(33);
		event(4.35);

		verfiyCulumateRoi(59.35);
	}

	private void event( final double percentageChange ) {

		final ReturnOnInvestmentEvent event = mock(ReturnOnInvestmentEvent.class);
		when(event.percentageChange()).thenReturn(BigDecimal.valueOf(percentageChange));

		calculator.event(event);
	}

	private void verfiyCulumateRoi( final double expectedCulumativeRoi ) {

		assertEquals(
		        String.format("%s != %s", expectedCulumativeRoi, calculator.cumulativeReturnOnInvestment()),
		        0,
		        BigDecimal.valueOf(expectedCulumativeRoi).compareTo(calculator.cumulativeReturnOnInvestment()));
	}
}
