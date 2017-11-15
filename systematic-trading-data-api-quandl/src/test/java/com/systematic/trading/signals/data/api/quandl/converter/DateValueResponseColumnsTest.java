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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.systematic.trading.data.exception.CannotRetrieveDataException;
import com.systematic.trading.signals.data.api.quandl.model.QuandlColumnName;

/**
 * Verifies AllResponseColumns behaviour.
 * 
 * @author CJ Hare
 */
public class DateValueResponseColumnsTest {

	private static final String DATE_COLUMN_NAME = "date";
	private static final String VALUE_COLUMN_NAME = "value";

	/** Instance being tested. */
	private ResponseColumns responseColumns;

	@Before
	public void setUp() {
		responseColumns = new DateValueResponseColumns();
	}

	@Test
	public void canParseNoColumns() throws CannotRetrieveDataException {
		final List<QuandlColumnName> columns = createNoColumns();

		final boolean parsable = canParse(columns);

		verfiyNotParsable(parsable);
	}

	@Test
	public void canParseAllColumns() throws CannotRetrieveDataException {
		final List<QuandlColumnName> columns = createAllColumns();

		final boolean parsable = canParse(columns);

		verfiyParsable(parsable);
	}

	@Test
	public void canParseMissingDateColumn() throws CannotRetrieveDataException {
		final List<QuandlColumnName> columns = createMissingDateColumn();

		final boolean parsable = canParse(columns);

		verfiyNotParsable(parsable);
	}

	@Test
	public void canParseMissingValueColumn() throws CannotRetrieveDataException {
		final List<QuandlColumnName> columns = createMissingValueColumn();

		final boolean parsable = canParse(columns);

		verfiyNotParsable(parsable);
	}

	@Test(expected = CannotRetrieveDataException.class)
	public void missingDateColumn() throws CannotRetrieveDataException {
		final List<QuandlColumnName> columns = createMissingDateColumn();

		dateIndex(columns);
	}

	@Test(expected = CannotRetrieveDataException.class)
	public void missingOpenPriceColumn() throws CannotRetrieveDataException {
		final List<QuandlColumnName> columns = createMissingValueColumn();

		openPriceIndex(columns);
	}

	@Test(expected = CannotRetrieveDataException.class)
	public void missingClosePriceColumn() throws CannotRetrieveDataException {
		final List<QuandlColumnName> columns = createMissingValueColumn();

		closePriceIndex(columns);
	}

	@Test(expected = CannotRetrieveDataException.class)
	public void missingHighPriceColumn() throws CannotRetrieveDataException {
		final List<QuandlColumnName> columns = createMissingValueColumn();

		highPriceIndex(columns);
	}

	@Test(expected = CannotRetrieveDataException.class)
	public void missingLowPriceColumn() throws CannotRetrieveDataException {
		final List<QuandlColumnName> columns = createMissingValueColumn();

		lowPriceIndex(columns);
	}

	@Test
	public void dateIndex() throws CannotRetrieveDataException {
		final List<QuandlColumnName> columns = createAllColumns();

		final int index = dateIndex(columns);

		verifyIndex(0, index);
	}

	@Test
	public void openPriceIndex() throws CannotRetrieveDataException {
		final List<QuandlColumnName> columns = createAllColumns();

		final int index = openPriceIndex(columns);

		verifyIndex(1, index);
	}

	@Test
	public void closePriceIndex() throws CannotRetrieveDataException {
		final List<QuandlColumnName> columns = createAllColumns();

		final int index = closePriceIndex(columns);

		verifyIndex(1, index);
	}

	@Test
	public void highPriceIndex() throws CannotRetrieveDataException {
		final List<QuandlColumnName> columns = createAllColumns();

		final int index = highPriceIndex(columns);

		verifyIndex(1, index);
	}

	@Test
	public void lowPriceIndex() throws CannotRetrieveDataException {
		final List<QuandlColumnName> columns = createAllColumns();

		final int index = lowPriceIndex(columns);

		verifyIndex(1, index);
	}

	private void verifyIndex( final int expected, final int actual ) {
		assertEquals(expected, actual);
	}

	private void verfiyNotParsable( final boolean parsable ) {
		assertEquals(false, parsable);
	}

	private void verfiyParsable( final boolean parsable ) {
		assertEquals(true, parsable);
	}

	private int dateIndex( final List<QuandlColumnName> columns ) throws CannotRetrieveDataException {
		return responseColumns.dateIndex(columns);
	}

	private int openPriceIndex( final List<QuandlColumnName> columns ) throws CannotRetrieveDataException {
		return responseColumns.openPriceIndex(columns);
	}

	private int closePriceIndex( final List<QuandlColumnName> columns ) throws CannotRetrieveDataException {
		return responseColumns.closePriceIndex(columns);
	}

	private int highPriceIndex( final List<QuandlColumnName> columns ) throws CannotRetrieveDataException {
		return responseColumns.highPriceIndex(columns);
	}

	private int lowPriceIndex( final List<QuandlColumnName> columns ) throws CannotRetrieveDataException {
		return responseColumns.lowPriceIndex(columns);
	}

	private boolean canParse( final List<QuandlColumnName> columns ) {
		return responseColumns.canParse(columns);
	}

	private List<QuandlColumnName> createNoColumns() {
		return new ArrayList<>();
	}

	private List<QuandlColumnName> createAllColumns() {
		return createColumns(DATE_COLUMN_NAME, VALUE_COLUMN_NAME);
	}

	private List<QuandlColumnName> createMissingDateColumn() {
		return createColumns(VALUE_COLUMN_NAME);
	}

	private List<QuandlColumnName> createMissingValueColumn() {
		return createColumns(DATE_COLUMN_NAME);
	}

	private List<QuandlColumnName> createColumns( final String... names ) {
		final List<QuandlColumnName> columns = new ArrayList<>();

		for (final String name : names) {
			columns.add(new QuandlColumnName(name));
		}

		return columns;
	}
}