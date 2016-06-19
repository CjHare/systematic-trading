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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.simulation.analysis.roi.CulmativeTotalReturnOnInvestmentCalculator;
import com.systematic.trading.simulation.analysis.roi.event.ReturnOnInvestmentEvent;

/**
 * Tests the behaviour of the CulmativeTotalReturnOnInvestmentCalculator.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class CulmativeTotalReturnOnInvestmentCalculatorTest {
	private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

	@Test
	public void noEvents() {
		final CulmativeTotalReturnOnInvestmentCalculator calculator = new CulmativeTotalReturnOnInvestmentCalculator(
		        MATH_CONTEXT);

		assertEquals(BigDecimal.ZERO, calculator.getCumulativeReturnOnInvestment());
	}

	@Test
	public void oneEvents() {
		final CulmativeTotalReturnOnInvestmentCalculator calculator = new CulmativeTotalReturnOnInvestmentCalculator(
		        MATH_CONTEXT);

		final BigDecimal expected = BigDecimal.valueOf(101);
		final ReturnOnInvestmentEvent eventOne = mock(ReturnOnInvestmentEvent.class);
		when(eventOne.getPercentageChange()).thenReturn(expected);

		calculator.event(eventOne);

		assertEquals(0, expected.compareTo(calculator.getCumulativeReturnOnInvestment()));
	}

	@Test
	public void threeEvents() {
		final CulmativeTotalReturnOnInvestmentCalculator calculator = new CulmativeTotalReturnOnInvestmentCalculator(
		        MATH_CONTEXT);

		final BigDecimal one = BigDecimal.valueOf(22);
		final ReturnOnInvestmentEvent eventOne = mock(ReturnOnInvestmentEvent.class);
		when(eventOne.getPercentageChange()).thenReturn(one);
		final BigDecimal two = BigDecimal.valueOf(33);
		final ReturnOnInvestmentEvent eventTwo = mock(ReturnOnInvestmentEvent.class);
		when(eventTwo.getPercentageChange()).thenReturn(two);
		final BigDecimal three = BigDecimal.valueOf(4.35);
		final ReturnOnInvestmentEvent eventThree = mock(ReturnOnInvestmentEvent.class);
		when(eventThree.getPercentageChange()).thenReturn(three);

		calculator.event(eventOne);
		calculator.event(eventTwo);
		calculator.event(eventThree);

		final BigDecimal expected = one.add(two, MATH_CONTEXT).add(three, MATH_CONTEXT);
		assertEquals(0, expected.compareTo(calculator.getCumulativeReturnOnInvestment()));
	}
}
