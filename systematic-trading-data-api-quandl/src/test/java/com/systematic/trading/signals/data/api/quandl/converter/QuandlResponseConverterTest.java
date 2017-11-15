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
package com.systematic.trading.signals.data.api.quandl.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.text.RandomStringGenerator;
import org.junit.Test;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.exception.CannotRetrieveDataException;
import com.systematic.trading.signals.data.api.quandl.resource.ColumnResource;
import com.systematic.trading.signals.data.api.quandl.resource.DatatableResource;

/**
 * Various conditions for parsing the Datatable response object from Quandl.
 * 
 * @author CJ Hare
 */
public class QuandlResponseConverterTest {

	@Test
	public void convertEmptyPayload() throws CannotRetrieveDataException {
		final String tickerSymbol = randomTickerSymbol();
		final DatatableResource datatable = createEmptyDatatable();

		final TradingDayPrices[] prices = new QuandlResponseConverter().convert(tickerSymbol, datatable);

		verifyPrices(prices);
	}

	@Test
	public void convertOneTuple() throws CannotRetrieveDataException {
		final String tickerSymbol = randomTickerSymbol();
		final DatatableResource datatable = createDatatableWithOneTuple();

		final TradingDayPrices[] prices = new QuandlResponseConverter().convert(tickerSymbol, datatable);

		verifyPrices(prices, new double[] { 2.5, 1.75, 3.25, 2.8 });
	}

	@Test
	public void convertSuffledColumns() throws CannotRetrieveDataException {
		final String tickerSymbol = randomTickerSymbol();
		final DatatableResource datatable = createDatatableSuffledColumns();

		final TradingDayPrices[] prices = new QuandlResponseConverter().convert(tickerSymbol, datatable);

		verifyPrices(prices, new double[] { 3.25, 2.5, 2.8, 1.75 });
	}

	@Test
	public void convertTwoTuples() throws CannotRetrieveDataException {
		final String tickerSymbol = randomTickerSymbol();
		final DatatableResource datatable = createDatatableWithTwoTuple();

		final TradingDayPrices[] prices = new QuandlResponseConverter().convert(tickerSymbol, datatable);

		verifyPrices(prices, new double[] { 2.8, 2.05, 3.99, 2.81, 2.5, 1.75, 3.25, 2.8 });
	}

	private DatatableResource createDatatableWithTwoTuple() throws CannotRetrieveDataException {
		final List<List<Object>> data = new ArrayList<>();
		data.add(createTuple("2012-01-23", 2.8, 2.05, 3.99, 2.81));
		data.add(createTuple("2012-01-24", 2.5, 1.75, 3.25, 2.8));

		return createDatatable(getStandardColumns(), data);
	}

	private DatatableResource createDatatableWithOneTuple() throws CannotRetrieveDataException {
		final List<List<Object>> data = new ArrayList<>();
		data.add(createTuple("2010-12-01", 2.5, 1.75, 3.25, 2.8));

		return createDatatable(getStandardColumns(), data);
	}

	private DatatableResource createDatatableSuffledColumns() throws CannotRetrieveDataException {
		final List<List<Object>> data = new ArrayList<>();
		data.add(createTuple("2010-12-01", 2.5, 1.75, 3.25, 2.8));

		return createDatatable(getShuffledColumns(), data);
	}

	private void verifyPrices( final TradingDayPrices[] prices, final double... expected ) {
		assertNotNull(prices);
		assertEquals("Number of prices does not match expectations", expected.length / 4, prices.length);

		for (int i = 0, j = 0; i < expected.length; i += 4, j++) {
			verifyBigDecimalEquals(expected[i], prices[j].getOpeningPrice().getPrice());
			verifyBigDecimalEquals(expected[i + 1], prices[j].getLowestPrice().getPrice());
			verifyBigDecimalEquals(expected[i + 2], prices[j].getHighestPrice().getPrice());
			verifyBigDecimalEquals(expected[i + 3], prices[j].getClosingPrice().getPrice());
		}
	}

	private void verifyBigDecimalEquals( final double expected, final BigDecimal actual ) {
		assertEquals(String.format("Expected %s != %s", expected, actual), 0,
		        BigDecimal.valueOf(expected).compareTo(actual));

	}

	private List<Object> createTuple( final String date, final double open, final double high, final double low,
	        final double close ) {
		final List<Object> tuple = new ArrayList<>();
		tuple.add(date);
		tuple.add(open);
		tuple.add(high);
		tuple.add(low);
		tuple.add(close);
		return tuple;
	}

	private List<ColumnResource> getStandardColumns() {
		final List<ColumnResource> columns = new ArrayList<>();
		columns.add(createColumn("date", "Date"));
		columns.add(createColumn("open", "BigDecimal(34,12)"));
		columns.add(createColumn("low", "BigDecimal(34,12)"));
		columns.add(createColumn("high", "BigDecimal(34,12)"));
		columns.add(createColumn("close", "BigDecimal(34,12)"));
		return columns;
	}

	private List<ColumnResource> getShuffledColumns() {
		final List<ColumnResource> columns = new ArrayList<>();
		columns.add(createColumn("date", "Date"));
		columns.add(createColumn("low", "BigDecimal(34,12)"));
		columns.add(createColumn("close", "BigDecimal(34,12)"));
		columns.add(createColumn("open", "BigDecimal(34,12)"));
		columns.add(createColumn("high", "BigDecimal(34,12)"));
		return columns;
	}

	private ColumnResource createColumn( final String name, final String type ) {
		final ColumnResource column = new ColumnResource();
		column.setName(name);
		column.setType(type);
		return column;
	}

	private DatatableResource createEmptyDatatable() throws CannotRetrieveDataException {
		return createDatatable(getStandardColumns(), new ArrayList<>());
	}

	private DatatableResource createDatatable( final List<ColumnResource> columns, final List<List<Object>> data )
	        throws CannotRetrieveDataException {
		final DatatableResource datatable = new DatatableResource();
		datatable.setData(data);
		datatable.setColumns(columns);
		return datatable;
	}

	/**
	 * Generates a 4 code point string, using only the letters a-z
	 */
	private String randomTickerSymbol() {
		return new RandomStringGenerator.Builder().withinRange('a', 'z').build().generate(4);
	}
}