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
package com.systematic.trading.backtest.brokerage;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.Period;

import org.junit.Test;

import com.systematic.trading.simulation.brokerage.MonthlyRollingCounter;

/**
 * Ensuring the Monthly Rolling Counter behaves correctly.
 * 
 * @author CJ Hare
 */
public class MonthlyRollingCounterTest {

	@Test
	public void addOne() {
		final MonthlyRollingCounter counter = new MonthlyRollingCounter();

		final int count = counter.add( LocalDate.now() );

		assertEquals( 1, count );
	}

	@Test
	public void addTwo() {
		final MonthlyRollingCounter counter = new MonthlyRollingCounter();

		counter.add( LocalDate.now() );
		final int count = counter.add( LocalDate.now() );

		assertEquals( 2, count );
	}

	@Test
	public void addTwoMonthSplit() {
		final MonthlyRollingCounter counter = new MonthlyRollingCounter();

		counter.add( LocalDate.now().minus( Period.ofMonths( 1 ) ) );
		final int count = counter.add( LocalDate.now() );

		assertEquals( 1, count );
	}

	@Test
	public void addTwoYearsSplit() {
		final MonthlyRollingCounter counter = new MonthlyRollingCounter();

		counter.add( LocalDate.now().minus( Period.ofYears( 1 ) ) );
		final int count = counter.add( LocalDate.now() );

		assertEquals( 1, count );
	}

	@Test
	public void addRolling() {
		final MonthlyRollingCounter counter = new MonthlyRollingCounter();

		// Here, then back (should dump that count) then here again
		counter.add( LocalDate.now() );
		counter.add( LocalDate.now().minus( Period.ofMonths( 1 ) ) );
		final int count = counter.add( LocalDate.now() );

		assertEquals( 1, count );
	}

	@Test
	public void getCountZero() {
		final MonthlyRollingCounter counter = new MonthlyRollingCounter();

		final int count = counter.get( LocalDate.now() );

		assertEquals( 0, count );
	}

	@Test
	public void getCountTwo() {
		final MonthlyRollingCounter counter = new MonthlyRollingCounter();

		counter.add( LocalDate.now() );
		counter.add( LocalDate.now() );
		final int count = counter.get( LocalDate.now() );

		assertEquals( 2, count );
	}

	@Test
	public void getRollingCounterLost() {
		final MonthlyRollingCounter counter = new MonthlyRollingCounter();

		counter.add( LocalDate.now().minus( Period.ofMonths( 2 ) ) );
		counter.add( LocalDate.now().minus( Period.ofMonths( 1 ) ) );
		final int count = counter.get( LocalDate.now().minus( Period.ofMonths( 2 ) ) );

		assertEquals( 0, count );
	}
}
