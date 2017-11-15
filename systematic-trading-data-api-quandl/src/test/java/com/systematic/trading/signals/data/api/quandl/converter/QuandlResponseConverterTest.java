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
import com.systematic.trading.signals.data.api.quandl.model.QuandlColumnName;
import com.systematic.trading.signals.data.api.quandl.model.QuandlResultSet;

/**
 * Various conditions for parsing the Datatable response object from Quandl.
 * 
 * @author CJ Hare
 */
public class QuandlResponseConverterTest {

	@Test
	public void convertEmptyPayload() throws CannotRetrieveDataException {
		final String tickerSymbol = randomTickerSymbol();
		final QuandlResultSet datatable = emptyResultSet();

		final TradingDayPrices[] prices = new QuandlResponseConverter().convert(tickerSymbol, datatable);

		verifyPrices(prices);
	}

	@Test
	public void convertOneTuple() throws CannotRetrieveDataException {
		final String tickerSymbol = randomTickerSymbol();
		final QuandlResultSet datatable = resultSetWithOneTuple();

		final TradingDayPrices[] prices = new QuandlResponseConverter().convert(tickerSymbol, datatable);

		verifyPrices(prices, new double[] { 2.5, 1.75, 3.25, 2.8 });
	}

	@Test
	public void convertSuffledColumns() throws CannotRetrieveDataException {
		final String tickerSymbol = randomTickerSymbol();
		final QuandlResultSet datatable = resultSetSuffledColumns();

		final TradingDayPrices[] prices = new QuandlResponseConverter().convert(tickerSymbol, datatable);

		verifyPrices(prices, new double[] { 3.25, 2.5, 2.8, 1.75 });
	}

	@Test
	public void convertTwoTuples() throws CannotRetrieveDataException {
		final String tickerSymbol = randomTickerSymbol();
		final QuandlResultSet datatable = resultSetWithTwoTuple();

		final TradingDayPrices[] prices = new QuandlResponseConverter().convert(tickerSymbol, datatable);

		verifyPrices(prices, new double[] { 2.8, 2.05, 3.99, 2.81, 2.5, 1.75, 3.25, 2.8 });
	}

	private QuandlResultSet resultSetWithTwoTuple() throws CannotRetrieveDataException {
		final List<List<Object>> data = new ArrayList<>();
		data.add(tuple("2012-01-23", 2.8, 2.05, 3.99, 2.81));
		data.add(tuple("2012-01-24", 2.5, 1.75, 3.25, 2.8));

		return new QuandlResultSet(standardColumns(), data);
	}

	private QuandlResultSet resultSetWithOneTuple() throws CannotRetrieveDataException {
		final List<List<Object>> data = new ArrayList<>();
		data.add(tuple("2010-12-01", 2.5, 1.75, 3.25, 2.8));

		return new QuandlResultSet(standardColumns(), data);
	}

	private QuandlResultSet resultSetSuffledColumns() throws CannotRetrieveDataException {
		final List<List<Object>> data = new ArrayList<>();
		data.add(tuple("2010-12-01", 2.5, 1.75, 3.25, 2.8));

		return new QuandlResultSet(shuffledColumns(), data);
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

	private List<Object> tuple( final String date, final double open, final double high, final double low,
	        final double close ) {
		final List<Object> tuple = new ArrayList<>();
		tuple.add(date);
		tuple.add(open);
		tuple.add(high);
		tuple.add(low);
		tuple.add(close);
		return tuple;
	}

	private List<QuandlColumnName> standardColumns() {
		final List<QuandlColumnName> columns = new ArrayList<>();
		columns.add(new QuandlColumnName("date"));
		columns.add(new QuandlColumnName("open"));
		columns.add(new QuandlColumnName("low"));
		columns.add(new QuandlColumnName("high"));
		columns.add(new QuandlColumnName("close"));
		return columns;
	}

	private List<QuandlColumnName> shuffledColumns() {
		final List<QuandlColumnName> columns = new ArrayList<>();
		columns.add(new QuandlColumnName("date"));
		columns.add(new QuandlColumnName("low"));
		columns.add(new QuandlColumnName("close"));
		columns.add(new QuandlColumnName("open"));
		columns.add(new QuandlColumnName("high"));
		return columns;
	}

	private QuandlResultSet emptyResultSet() throws CannotRetrieveDataException {
		return new QuandlResultSet(standardColumns(), new ArrayList<>());
	}

	/**
	 * Generates a 4 code point string, using only the letters a-z
	 */
	private String randomTickerSymbol() {
		return new RandomStringGenerator.Builder().withinRange('a', 'z').build().generate(4);
	}
}