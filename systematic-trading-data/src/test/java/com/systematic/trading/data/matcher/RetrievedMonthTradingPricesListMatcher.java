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
package com.systematic.trading.data.matcher;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

import com.systematic.trading.data.model.RetrievedMonthTradingPrices;

/**
 * Matcher for a list of RetrievedMonthTradingPrices, contained in any order.
 * 
 * @author CJ Hare
 */
public class RetrievedMonthTradingPricesListMatcher extends ArgumentMatcher<List<RetrievedMonthTradingPrices>> {

	private final List<RetrievedMonthTradingPrices> retrieved;

	public RetrievedMonthTradingPricesListMatcher( final List<RetrievedMonthTradingPrices> retrieved ) {

		this.retrieved = retrieved;
	}

	public boolean matches( Object argument ) {

		if (argument instanceof List<?>) {
			@SuppressWarnings("unchecked") final List<RetrievedMonthTradingPrices> actualValues = (List<RetrievedMonthTradingPrices>) argument;

			if (retrieved.size() != actualValues.size()) { return false; }

			for (final RetrievedMonthTradingPrices expected : retrieved) {
				final String expectedTickerSymbol = expected.tickerSymbol();
				final int expectedYear = expected.yearMonth().getYear();
				final int expectedMonth = expected.yearMonth().getMonthValue();

				// At least one match needs to be in the actual array of values
				if (!hasMatch(expectedTickerSymbol, expectedYear, expectedMonth, actualValues)) { return false; }
			}

			return true;
		}

		return false;
	}

	@Override
	public void describeTo( Description description ) {

		description.appendText(retrieved.stream().map(r -> r.toString()).collect(Collectors.joining(", ")));
	}

	private boolean hasMatch( final String expectedTickerSymbol, final int expectedYear, final int expectedMonth,
	        final List<RetrievedMonthTradingPrices> actualValues ) {

		boolean matched = false;

		for (final RetrievedMonthTradingPrices actual : actualValues) {
			matched = StringUtils.equals(expectedTickerSymbol, actual.tickerSymbol())
			        && expectedYear == actual.yearMonth().getYear()
			        && expectedMonth == actual.yearMonth().getMonthValue();

			if (matched) {
				break;
			}
		}

		return matched;
	}
}