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
package com.systematic.trading.maths.matcher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.SortedMap;

import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

/**
 * Matches on the contents of a list of BigDecimal values.
 * 
 * @author CJ Hare
 */
public class IsSortedMap extends ArgumentMatcher<SortedMap<LocalDate, BigDecimal>> {

	private final SortedMap<LocalDate, BigDecimal> expected;

	public IsSortedMap( final SortedMap<LocalDate, BigDecimal> values ) {
		this.expected = values;
	}

	@Override
	public boolean matches( final Object argument ) {

		if (argument instanceof SortedMap<?, ?>) {

			@SuppressWarnings("unchecked")
			final SortedMap<LocalDate, BigDecimal> given = (SortedMap<LocalDate, BigDecimal>) argument;

			for (final Map.Entry<LocalDate, BigDecimal> entry : expected.entrySet()) {

				if (isMissingEntry(entry, given)) {
					return false;
				}
			}

			return true;
		}

		return false;
	}

	private boolean isMissingEntry( Map.Entry<LocalDate, BigDecimal> entry,
	        final SortedMap<LocalDate, BigDecimal> given ) {

		return !given.containsKey(entry.getKey()) || entry.getValue().compareTo(given.get(entry.getKey())) != 0;
	}

	@Override
	public void describeTo( final Description description ) {

		description.appendText(expected.toString());
	}
}