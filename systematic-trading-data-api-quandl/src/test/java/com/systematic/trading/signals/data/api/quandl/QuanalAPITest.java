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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.text.RandomStringGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.api.exception.CannotRetrieveDataException;
import com.systematic.trading.signals.data.api.quandl.dao.QuandlDao;
import com.systematic.trading.signals.data.api.quandl.model.ColumnResource;
import com.systematic.trading.signals.data.api.quandl.model.DatatableResource;
import com.systematic.trading.signals.data.api.quandl.model.QuandlResponseResource;

/**
 * Quandl API.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class QuanalAPITest {

	@Mock
	private QuandlDao dao;

	@Test
	public void maximumRetrievalPeriodPerCall() {
		final Period actual = new QuandlAPI(dao).getMaximumDurationInSingleUpdate();

		verfiyMaximumRetrieval(actual);
		verifyNoQuandlCall();
	}

	@Test
	public void getStockDataEmptyPayload() throws CannotRetrieveDataException {
		setUpEmptyQuandlResponse();

		final String tickerSymbol = randomTickerSymbol();
		final LocalDate inclusiveStartDate = randomStartDate();
		final LocalDate exclusiveEndDate = randomEndDate(inclusiveStartDate);

		final TradingDayPrices[] prices = new QuandlAPI(dao).getStockData(tickerSymbol, inclusiveStartDate,
		        exclusiveEndDate);

		verifyTradingDayPrices(prices);
		verifyQuandlCall(tickerSymbol, inclusiveStartDate, exclusiveEndDate);
	}

	private void verifyTradingDayPrices( final TradingDayPrices[] prices ) {
		assertNotNull(prices);
	}

	private void verifyQuandlCall( final String tickerSymbol, final LocalDate inclusiveStartDate,
	        final LocalDate exclusiveEndDate ) throws CannotRetrieveDataException {
		verify(dao).get(tickerSymbol, inclusiveStartDate, exclusiveEndDate);
		verifyNoMoreInteractions(dao);
	}

	/**
	 * Response containing the structure but no data.
	 */
	private void setUpEmptyQuandlResponse() throws CannotRetrieveDataException {
		final QuandlResponseResource response = new QuandlResponseResource();
		final DatatableResource datatable = new DatatableResource();
		final List<ColumnResource> columns = new ArrayList<>();
		final List<List<Object>> data = new ArrayList<>();
		datatable.setData(data);
		datatable.setColumns(columns);
		response.setDatatable(datatable);

		when(dao.get(anyString(), any(LocalDate.class), any(LocalDate.class))).thenReturn(response);
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

	private void verfiyMaximumRetrieval( final Period actual ) {
		assertEquals(Period.ofYears(1), actual);
	}

	private void verifyNoQuandlCall() {
		verifyZeroInteractions(dao);
	}
}