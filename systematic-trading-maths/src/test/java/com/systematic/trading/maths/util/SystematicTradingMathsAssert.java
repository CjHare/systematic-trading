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
package com.systematic.trading.maths.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * Utility Assert operations.
 * 
 * @author CJ Hare
 */
public class SystematicTradingMathsAssert {

	private static final int TWO_DECIMAL_PLACES = 2;

	public static void assertValue( final double expected, final Collection<BigDecimal> actual ) {
		assertNotNull("Need an actual/results array", actual);
		assertEquals("Actual length > != 1", 1, actual.size());

		final BigDecimal firstEntry = actual.iterator().next();

		assertEquals(String.format("%s != %s", expected, firstEntry), 0,
		        BigDecimal.valueOf(expected).compareTo(firstEntry));
	}

	public static void assertValues( final double[] expected, final List<BigDecimal> actual ) {
		assertArraySizeEqual(expected, actual);

		for (int i = 0; i < expected.length; i++) {
			assertBigDecimalEquals(expected, actual, i);
		}
	}

	public static void assertValues( final double[] expected, final SortedMap<LocalDate, BigDecimal> actual ) {
		assertArraySizeEqual(expected, actual);

		int i = 0;
		for (final Map.Entry<LocalDate, BigDecimal> entry : actual.entrySet()) {
			assertBigDecimalEquals(expected[i], entry.getValue(), RoundingMode.HALF_EVEN);
			i++;
		}
	}

	public static void assertValuesTwoDecimalPlaces( final double[] expected,
	        final Map<LocalDate, BigDecimal> actual ) {
		assertArraySizeEqual(expected, actual);
		int index = 0;

		for (final Map.Entry<LocalDate, BigDecimal> entry : actual.entrySet()) {
			assertBigDecimalEquals(expected[index], entry.getValue(), RoundingMode.HALF_EVEN);
			index++;
		}
	}

	public static void assertValuesTwoDecimalPlaces( final double[] expected, final List<BigDecimal> actual ) {
		assertArraySizeEqual(expected, actual);

		for (int i = 0; i < expected.length; i++) {
			assertBigDecimalEquals(expected, actual, i, RoundingMode.HALF_EVEN);
		}
	}

	public static void assertBigDecimalEquals( final double expected, BigDecimal actual ) {
		assertEquals(String.format("%s != %s", expected, actual), 0, BigDecimal.valueOf(expected).compareTo(actual));
	}

	private static void assertBigDecimalEquals( final double[] expected, final List<BigDecimal> actual, final int i ) {
		assertEquals(String.format("%s != %s", expected[i], actual.get(i)), 0,
		        BigDecimal.valueOf(expected[i]).compareTo(actual.get(i)));
	}

	private static void assertBigDecimalEquals( final double[] expected, final List<BigDecimal> actual, final int i,
	        final RoundingMode mode ) {
		assertBigDecimalEquals(expected[i], actual.get(i), mode);
	}

	private static void assertBigDecimalEquals( final double expected, final BigDecimal actual,
	        final RoundingMode mode ) {
		assertEquals(String.format("%s != %s", expected, actual), 0,
		        BigDecimal.valueOf(expected).compareTo(actual.setScale(TWO_DECIMAL_PLACES, mode)));
	}

	private static void assertArraySizeEqual( final double[] expected, final Collection<?> actual ) {
		assertNotNull("Need an expected array", expected);
		assertNotNull("Need an actual/results array", actual);
		assertEquals("Actual length != Expected length", expected.length, actual.size());
	}

	private static void assertArraySizeEqual( final double[] expected, final Map<?, ?> actual ) {
		assertNotNull("Need an expected array", expected);
		assertNotNull("Need an actual/results array", actual);
		assertEquals("Actual length != Expected length", expected.length, actual.size());
	}
}