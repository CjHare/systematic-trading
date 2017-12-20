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
package com.systematic.trading.simulation.brokerage;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.Period;

import org.junit.Before;
import org.junit.Test;

import com.systematic.trading.simulation.brokerage.MonthlyRollingCounter;

/**
 * Ensuring the Monthly Rolling Counter behaves correctly.
 * 
 * @author CJ Hare
 */
public class MonthlyRollingCounterTest {

	private MonthlyRollingCounter counter;

	@Before
	public void setUp() {

		counter = new MonthlyRollingCounter();
	}

	@Test
	public void addOne() {

		incrementCurrentMonth();

		verifyCurrentMonthCount(1);
	}

	@Test
	public void addTwo() {

		incrementCurrentMonth();
		incrementCurrentMonth();

		verifyCurrentMonthCount(2);
	}

	@Test
	public void addTwoMonthSplit() {

		incrementreviousMonth();
		incrementCurrentMonth();

		verifyCurrentMonthCount(1);
	}

	@Test
	public void addTwoYearsSplit() {

		incrementPreviousYear();
		incrementCurrentMonth();

		verifyCurrentMonthCount(1);
	}

	@Test
	public void addRolling() {

		// Here, then back (should dump that count) then here again
		incrementCurrentMonth();
		incrementreviousMonth();
		incrementCurrentMonth();

		verifyCurrentMonthCount(1);
	}

	@Test
	public void countZero() {

		verifyCurrentMonthCount(0);
	}

	@Test
	public void rollingCounterLost() {

		incrementreviousMonth();
		incrementCurrentMonth();

		veriyfPreviousMonthCount(0);
	}

	private void incrementCurrentMonth() {

		counter.add(LocalDate.now());
	}

	private void incrementreviousMonth() {

		counter.add(LocalDate.now().minus(Period.ofMonths(1)));
	}

	private void incrementPreviousYear() {

		counter.add(LocalDate.now().minus(Period.ofYears(1)));
	}

	private void verifyCurrentMonthCount( final int expected ) {

		assertEquals(expected, counter.get(LocalDate.now()));
	}

	private void veriyfPreviousMonthCount( final int expected ) {

		assertEquals(expected, counter.get(LocalDate.now().minus(Period.ofMonths(1))));
	}
}