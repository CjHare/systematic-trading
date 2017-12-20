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
package com.systematic.trading.signals.data.api.quandl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Period;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.text.RandomStringGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.collections.BlockingEventCount;
import com.systematic.trading.data.exception.CannotRetrieveDataException;
import com.systematic.trading.signals.data.api.quandl.configuration.QuandlConfiguration;
import com.systematic.trading.signals.data.api.quandl.converter.QuandlResponseConverter;
import com.systematic.trading.signals.data.api.quandl.dao.QuandlApiDao;
import com.systematic.trading.signals.data.api.quandl.model.QuandlResultSet;

/**
 * Quandl API.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class QuanalAPITest {

	@Mock
	private QuandlApiDao dao;

	@Mock
	private QuandlConfiguration configuration;

	@Mock
	private QuandlResponseConverter dataFormat;

	@Mock
	private BlockingEventCount throttler;

	@Test
	public void maximumRetrievalPeriodPerCall() {

		final Period expected = setUpMaximumRetrieval();

		final Period actual = new QuandlAPI(dao, configuration, dataFormat).maximumDurationPerConnection();

		verfiyMaximumRetrieval(expected, actual);
		verifyNoQuandlCall();
	}

	@Test
	public void stockDataEmptyPayload() throws CannotRetrieveDataException {

		final TradingDayPrices[] expectedPrices = setUpQuandlResponse();

		final String datasetId = randomDatasetId();
		final String tickerSymbol = randomTickerSymbol();
		final LocalDate inclusiveStartDate = randomStartDate();
		final LocalDate exclusiveEndDate = randomEndDate(inclusiveStartDate);

		final TradingDayPrices[] prices = callQuandl(datasetId, tickerSymbol, inclusiveStartDate, exclusiveEndDate);

		verifyTradingDayPrices(expectedPrices, prices);
		verifyQuandlCall(datasetId, tickerSymbol, inclusiveStartDate, exclusiveEndDate);
	}

	private TradingDayPrices[] callQuandl( final String datasetId, final String tickerSymbol,
	        final LocalDate inclusiveStartDate, final LocalDate exclusiveEndDate ) throws CannotRetrieveDataException {

		return new QuandlAPI(dao, configuration, dataFormat).stockData(datasetId, tickerSymbol, inclusiveStartDate,
		        exclusiveEndDate, throttler);
	}

	private void verifyTradingDayPrices( final TradingDayPrices[] expected, final TradingDayPrices[] actual ) {

		assertNotNull(actual);
		assertEquals("Number of prices does not match expectations", expected.length, actual.length);

		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], actual[i]);
		}
	}

	private void verifyQuandlCall( final String datasetId, final String tickerSymbol,
	        final LocalDate inclusiveStartDate, final LocalDate exclusiveEndDate ) throws CannotRetrieveDataException {

		verify(dao).get(datasetId, tickerSymbol, inclusiveStartDate, exclusiveEndDate, throttler);
		verifyNoMoreInteractions(dao);
		verifyNoMoreInteractions(throttler);
	}

	private TradingDayPrices[] setUpQuandlResponse() throws CannotRetrieveDataException {

		final QuandlResultSet results = mock(QuandlResultSet.class);
		when(dao.get(anyString(), anyString(), any(LocalDate.class), any(LocalDate.class),
		        any(BlockingEventCount.class))).thenReturn(results);

		final TradingDayPrices[] prices = new TradingDayPrices[2];
		prices[0] = mock(TradingDayPrices.class);
		prices[1] = mock(TradingDayPrices.class);
		when(dataFormat.convert(anyString(), eq(results))).thenReturn(prices);
		return prices;
	}

	/**
	 * Ends after a random period (1-100 days) after the start.
	 */
	private LocalDate randomEndDate( final LocalDate startDate ) {

		final long minDay = startDate.toEpochDay() + 1;
		final long maxDay = startDate.toEpochDay() + 100;
		final long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
		return LocalDate.ofEpochDay(randomDay);
	}

	private LocalDate randomStartDate() {

		final long minDay = LocalDate.of(1970, 1, 1).toEpochDay();
		final long maxDay = LocalDate.now().toEpochDay();
		final long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
		return LocalDate.ofEpochDay(randomDay);
	}

	/**
	 * Generates a 4 code point string, using only the letters a-z
	 */
	private String randomTickerSymbol() {

		return new RandomStringGenerator.Builder().withinRange('a', 'z').build().generate(4);
	}

	/**
	 * Generates a 4 code point string, using only the letters a-z
	 */
	private String randomDatasetId() {

		return new RandomStringGenerator.Builder().withinRange('a', 'z').build().generate(4);
	}

	/**
	 * Generates a random number of months as the maximum to retrieve.
	 */
	private Period setUpMaximumRetrieval() {

		final int months = new Random().nextInt(12);
		when(configuration.maximumMonthsPerConnection()).thenReturn(months);
		return Period.ofMonths(months);
	}

	private void verfiyMaximumRetrieval( final Period expected, final Period actual ) {

		assertEquals("Number of months to retrieve does not match", expected, actual);
	}

	private void verifyNoQuandlCall() {

		verifyZeroInteractions(dao);
		verifyZeroInteractions(throttler);
	}
}