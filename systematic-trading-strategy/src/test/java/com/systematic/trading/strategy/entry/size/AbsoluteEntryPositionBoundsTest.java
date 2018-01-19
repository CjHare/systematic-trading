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
package com.systematic.trading.strategy.entry.size;

import static com.systematic.trading.strategy.util.SystematicTradingStrategyAssert.assertBigDecimalEquals;

import java.math.BigDecimal;

import org.junit.Test;

/**
 * Verifying the AbsoluteEntryPositionBounds.
 * 
 * @author CJ Hare
 */
public class AbsoluteEntryPositionBoundsTest {

	private EntryPositionBounds bounds;

	@Test
	public void boundsInteger() {

		setUpBounds(55);

		final BigDecimal positionSize = bounds();

		verifyPositionSize(55, positionSize);
	}

	@Test
	public void boundsDecimal() {

		setUpBounds(12.45);

		final BigDecimal positionSize = bounds();

		verifyPositionSize(12.45, positionSize);
	}

	private void verifyPositionSize( final double expected, final BigDecimal actual ) {

		assertBigDecimalEquals(expected, actual);
	}

	private BigDecimal bounds() {

		return bounds.bounds(BigDecimal.ZERO);
	}

	private void setUpBounds( final double value ) {

		bounds = new AbsoluteEntryPositionBounds(BigDecimal.valueOf(value));
	}
}