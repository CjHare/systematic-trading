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
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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

		final TradingDayPrices[] prices = callQuandl(tickerSymbol, inclusiveStartDate, exclusiveEndDate);

		verifyTradingDayPrices(prices);
		verifyQuandlCall(tickerSymbol, inclusiveStartDate, exclusiveEndDate);
	}

	@Test
	public void getStockDataMissingDateColumn() throws CannotRetrieveDataException {
		setUpMissingDateColumnQuandlResponse();

		final String tickerSymbol = randomTickerSymbol();
		final LocalDate inclusiveStartDate = randomStartDate();
		final LocalDate exclusiveEndDate = randomEndDate(inclusiveStartDate);

		callQuandlExpectingMissingColumn("date", tickerSymbol, inclusiveStartDate, exclusiveEndDate);

		verifyQuandlCall(tickerSymbol, inclusiveStartDate, exclusiveEndDate);
	}

	@Test
	public void getStockDataMissingOpenColumn() throws CannotRetrieveDataException {
		setUpMissingOpenColumnQuandlResponse();

		final String tickerSymbol = randomTickerSymbol();
		final LocalDate inclusiveStartDate = randomStartDate();
		final LocalDate exclusiveEndDate = randomEndDate(inclusiveStartDate);

		callQuandlExpectingMissingColumn("open", tickerSymbol, inclusiveStartDate, exclusiveEndDate);

		verifyQuandlCall(tickerSymbol, inclusiveStartDate, exclusiveEndDate);
	}

	@Test
	public void getStockDataMissingLowColumn() throws CannotRetrieveDataException {
		setUpMissingLowColumnQuandlResponse();

		final String tickerSymbol = randomTickerSymbol();
		final LocalDate inclusiveStartDate = randomStartDate();
		final LocalDate exclusiveEndDate = randomEndDate(inclusiveStartDate);

		callQuandlExpectingMissingColumn("low", tickerSymbol, inclusiveStartDate, exclusiveEndDate);

		verifyQuandlCall(tickerSymbol, inclusiveStartDate, exclusiveEndDate);
	}

	@Test
	public void getStockDataMissingHighColumn() throws CannotRetrieveDataException {
		setUpMissingHighColumnQuandlResponse();

		final String tickerSymbol = randomTickerSymbol();
		final LocalDate inclusiveStartDate = randomStartDate();
		final LocalDate exclusiveEndDate = randomEndDate(inclusiveStartDate);

		callQuandlExpectingMissingColumn("high", tickerSymbol, inclusiveStartDate, exclusiveEndDate);

		verifyQuandlCall(tickerSymbol, inclusiveStartDate, exclusiveEndDate);
	}

	@Test
	public void getStockDataMissingCloseColumn() throws CannotRetrieveDataException {
		setUpMissingCloseColumnQuandlResponse();

		final String tickerSymbol = randomTickerSymbol();
		final LocalDate inclusiveStartDate = randomStartDate();
		final LocalDate exclusiveEndDate = randomEndDate(inclusiveStartDate);

		callQuandlExpectingMissingColumn("close", tickerSymbol, inclusiveStartDate, exclusiveEndDate);

		verifyQuandlCall(tickerSymbol, inclusiveStartDate, exclusiveEndDate);
	}

	private TradingDayPrices[] callQuandl( final String tickerSymbol, final LocalDate inclusiveStartDate,
	        final LocalDate exclusiveEndDate ) throws CannotRetrieveDataException {
		return new QuandlAPI(dao).getStockData(tickerSymbol, inclusiveStartDate, exclusiveEndDate);
	}

	private void callQuandlExpectingMissingColumn( final String name, final String tickerSymbol,
	        final LocalDate inclusiveStartDate, final LocalDate exclusiveEndDate ) {
		try {
			new QuandlAPI(dao).getStockData(tickerSymbol, inclusiveStartDate, exclusiveEndDate);
			fail("Expecting exception");
		} catch (final CannotRetrieveDataException e) {
			verifyMissingColumnMessage(name, e);
		}
	}

	private void verifyMissingColumnMessage( final String name, final Exception e ) {
		assertEquals(String.format("Missing expected column: %s", name), e.getMessage());
	}

	private void verifyTradingDayPrices( final TradingDayPrices[] prices ) {
		assertNotNull(prices);
	}

	private void verifyQuandlCall( final String tickerSymbol, final LocalDate inclusiveStartDate,
	        final LocalDate exclusiveEndDate ) throws CannotRetrieveDataException {
		verify(dao).get(tickerSymbol, inclusiveStartDate, exclusiveEndDate);
		verifyNoMoreInteractions(dao);
	}

	private List<List<Object>> getStandardData() {
		final List<List<Object>> data = new ArrayList<>();
		data.add(createTuple(LocalDate.now(), 2.5, 1.75, 3.25, 2.8));
		data.add(createTuple(LocalDate.now(), 2.8, 2.05, 3.99, 2.81));
		return data;
	}

	private List<Object> createTuple( final LocalDate date, final double open, final double high, final double low,
	        final double close ) {
		final List<Object> tuple = new ArrayList<>();
		tuple.add(date);
		tuple.add(BigDecimal.valueOf(open));
		tuple.add(BigDecimal.valueOf(high));
		tuple.add(BigDecimal.valueOf(low));
		tuple.add(BigDecimal.valueOf(close));
		return tuple;
	}

	private List<ColumnResource> getStandardColumns() {
		final List<ColumnResource> columns = new ArrayList<>();
		columns.add(createColumn("date", "Date"));
		columns.add(createColumn("open", "BigDecimal(34,12)"));
		columns.add(createColumn("high", "BigDecimal(34,12)"));
		columns.add(createColumn("low", "BigDecimal(34,12)"));
		columns.add(createColumn("close", "BigDecimal(34,12)"));
		return columns;
	}

	private ColumnResource createColumn( final String name, final String type ) {
		final ColumnResource column = new ColumnResource();
		column.setName(name);
		column.setType(type);
		return column;
	}

	private void setUpMissingDateColumnQuandlResponse() throws CannotRetrieveDataException {
		final List<ColumnResource> columns = new ArrayList<>();
		columns.add(createColumn("open", "BigDecimal(34,12)"));
		columns.add(createColumn("high", "BigDecimal(34,12)"));
		columns.add(createColumn("low", "BigDecimal(34,12)"));
		columns.add(createColumn("close", "BigDecimal(34,12)"));

		setUpQandlResponse(columns, new ArrayList<>());
	}

	private void setUpMissingOpenColumnQuandlResponse() throws CannotRetrieveDataException {
		final List<ColumnResource> columns = new ArrayList<>();
		columns.add(createColumn("date", "Date"));
		columns.add(createColumn("high", "BigDecimal(34,12)"));
		columns.add(createColumn("low", "BigDecimal(34,12)"));
		columns.add(createColumn("close", "BigDecimal(34,12)"));

		setUpQandlResponse(columns, new ArrayList<>());
	}

	private void setUpMissingLowColumnQuandlResponse() throws CannotRetrieveDataException {
		final List<ColumnResource> columns = new ArrayList<>();
		columns.add(createColumn("date", "Date"));
		columns.add(createColumn("open", "BigDecimal(34,12)"));
		columns.add(createColumn("high", "BigDecimal(34,12)"));
		columns.add(createColumn("close", "BigDecimal(34,12)"));

		setUpQandlResponse(columns, new ArrayList<>());
	}

	private void setUpMissingHighColumnQuandlResponse() throws CannotRetrieveDataException {
		final List<ColumnResource> columns = new ArrayList<>();
		columns.add(createColumn("date", "Date"));
		columns.add(createColumn("open", "BigDecimal(34,12)"));
		columns.add(createColumn("low", "BigDecimal(34,12)"));
		columns.add(createColumn("close", "BigDecimal(34,12)"));

		setUpQandlResponse(columns, new ArrayList<>());
	}

	private void setUpMissingCloseColumnQuandlResponse() throws CannotRetrieveDataException {
		final List<ColumnResource> columns = new ArrayList<>();
		columns.add(createColumn("date", "Date"));
		columns.add(createColumn("open", "BigDecimal(34,12)"));
		columns.add(createColumn("high", "BigDecimal(34,12)"));
		columns.add(createColumn("low", "BigDecimal(34,12)"));

		setUpQandlResponse(columns, new ArrayList<>());
	}

	private void setUpEmptyQuandlResponse() throws CannotRetrieveDataException {
		setUpQandlResponse(getStandardColumns(), new ArrayList<>());
	}

	private void setUpQandlResponse( final List<ColumnResource> columns, final List<List<Object>> data )
	        throws CannotRetrieveDataException {
		final QuandlResponseResource response = new QuandlResponseResource();
		final DatatableResource datatable = new DatatableResource();
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