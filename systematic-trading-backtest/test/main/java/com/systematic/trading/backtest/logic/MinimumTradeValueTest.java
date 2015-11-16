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
package com.systematic.trading.backtest.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;

import org.junit.Test;

/**
 * MinimumTradeValue test.
 * 
 * @author CJ Hare
 */
public class MinimumTradeValueTest {

	@Test
	public void getValue() {
		final BigDecimal expectedValue = BigDecimal.valueOf( 888.2223213212 );
		final MinimumTradeValue value = new MinimumTradeValue( expectedValue );

		assertNotNull( value );
		assertEquals( expectedValue, value.getValue() );
	}

	@Test
	public void isLessThanTrue() {
		final BigDecimal expectedValue = BigDecimal.valueOf( 888.2223213212 );
		final MinimumTradeValue value = new MinimumTradeValue( expectedValue );

		final boolean isLessThan = value.isLessThan( BigDecimal.valueOf( 888.3 ) );

		assertEquals( true, isLessThan );
	}

	@Test
	public void isLessThanFalse() {
		final BigDecimal expectedValue = BigDecimal.valueOf( 888.2223213212 );
		final MinimumTradeValue value = new MinimumTradeValue( expectedValue );

		final boolean isLessThan = value.isLessThan( expectedValue );

		assertEquals( false, isLessThan );
	}
}
