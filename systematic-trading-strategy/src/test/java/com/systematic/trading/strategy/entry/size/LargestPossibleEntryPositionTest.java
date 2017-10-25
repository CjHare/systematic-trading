/**
 * Copyright (c) 2015-2017, CJ Hare
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of [project] nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.systematic.trading.strategy.entry.size;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.simulation.cash.CashAccount;
import com.systematic.trading.strategy.definition.EntrySize;
import com.systematic.trading.strategy.matcher.BigDecimalMatcher;

/**
 * Verifies the behaviour of LargestPossibleEntryPosition
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class LargestPossibleEntryPositionTest {

	@Mock
	private CapitalEntryPositionBounds minimum;

	@Mock
	private CapitalEntryPositionBounds maximum;

	@Mock
	private CashAccount cashAccount;

	private EntrySize entrySize;

	@Before
	public void setUp() {
		entrySize = new LargestPossibleEntryPosition(minimum, maximum);
	}

	@Test
	/**
	 * No position when below the minimum
	 */
	public void belowMinimm() {
		setUpCashAccount(10);
		setUpMinimumBounds(55);

		final BigDecimal size = entryPosition();

		verifyPosition(0, size);
		verifyGetBalance();
		verifyMinimumBounds(10);
		verifyNoMaximumBounds();
	}

	@Test
	public void onMinimumBelowMaximum() {
		setUpCashAccount(32);
		setUpMinimumBounds(32);
		setUpMaximumBounds(100);

		final BigDecimal size = entryPosition();

		verifyPosition(32, size);
		verifyGetBalance();
		verifyMinimumBounds(32);
		verifyMaximumBounds(32);
	}

	@Test
	public void aboveMinimumBelowMaximum() {
		setUpCashAccount(45);
		setUpMinimumBounds(20);
		setUpMaximumBounds(90);

		final BigDecimal size = entryPosition();

		verifyPosition(45, size);
		verifyGetBalance();
		verifyMinimumBounds(45);
		verifyMaximumBounds(45);
	}

	@Test
	public void aboveMinimumAboveMaximum() {
		setUpCashAccount(125);
		setUpMinimumBounds(40);
		setUpMaximumBounds(80);

		final BigDecimal size = entryPosition();

		verifyPosition(80, size);
		verifyGetBalance();
		verifyMinimumBounds(125);
		verifyMaximumBounds(125);
	}

	@Test
	public void minimumAboveMaximum() {
		setUpCashAccount(125);
		setUpMinimumBounds(100);
		setUpMaximumBounds(50);

		final BigDecimal size = entryPosition();

		verifyPosition(100, size);
		verifyGetBalance();
		verifyMinimumBounds(125);
		verifyMaximumBounds(125);
	}

	@Test
	public void minimumNegative() {
		setUpCashAccount(125);
		setUpMinimumBounds(-1);
		setUpMaximumBounds(50);

		final BigDecimal size = entryPosition();

		verifyPosition(50, size);
		verifyGetBalance();
		verifyMinimumBounds(125);
		verifyMaximumBounds(125);
	}

	@Test
	public void maximumNegative() {
		setUpCashAccount(125);
		setUpMinimumBounds(24);
		setUpMaximumBounds(-1);

		final BigDecimal size = entryPosition();

		verifyPosition(24, size);
		verifyGetBalance();
		verifyMinimumBounds(125);
		verifyMaximumBounds(125);
	}

	private void verifyPosition( final double expected, final BigDecimal actual ) {
		assertBigDecimalEquals(expected, actual);
	}

	private void verifyGetBalance() {
		verify(cashAccount, atLeastOnce()).getBalance();
		verifyNoMoreInteractions(cashAccount);
	}

	private void verifyMinimumBounds( final double expected ) {
		verify(minimum).bounds(BigDecimalMatcher.argumentMatches(expected));
		verifyNoMoreInteractions(minimum);
	}

	private void verifyMaximumBounds( final double expected ) {
		verify(maximum).bounds(BigDecimalMatcher.argumentMatches(expected));
		verifyNoMoreInteractions(maximum);
	}

	private void verifyNoMaximumBounds() {
		verifyZeroInteractions(maximum);
	}

	private BigDecimal entryPosition() {
		return entrySize.entryPositionSize(cashAccount);
	}

	private void setUpCashAccount( final double balance ) {
		when(cashAccount.getBalance()).thenReturn(BigDecimal.valueOf(balance));
	}

	private void setUpMinimumBounds( final double amount ) {
		when(minimum.bounds(any(BigDecimal.class))).thenReturn(BigDecimal.valueOf(amount));
	}

	private void setUpMaximumBounds( final double amount ) {
		when(maximum.bounds(any(BigDecimal.class))).thenReturn(BigDecimal.valueOf(amount));
	}

	//TODO into a utility method
	public static void assertBigDecimalEquals( final double expected, BigDecimal actual ) {
		assertEquals(String.format("%s != %s", expected, actual), 0, BigDecimal.valueOf(expected).compareTo(actual));
	}
}