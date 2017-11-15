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

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.systematic.trading.data.exception.CannotRetrieveDataException;
import com.systematic.trading.signals.data.api.quandl.model.QuandlColumnName;

/**
 * Only the date and the single columns are present.
 * 
 * @author CJ Hare
 */
public class DateValueResponseColumns implements ResponseColumns {

	private static final String DATE_COLUMN_NAME = "date";
	private static final String VALUE_COLUMN_NAME = "value";

	@Override
	public boolean canParse( final List<QuandlColumnName> columns ) {
		return containsColumnName(DATE_COLUMN_NAME, columns) && containsColumnName(VALUE_COLUMN_NAME, columns);
	}

	@Override
	public int dateIndex( final List<QuandlColumnName> columns ) throws CannotRetrieveDataException {
		return getIndexOf(columns, DATE_COLUMN_NAME);
	}

	@Override
	public int openPriceIndex( final List<QuandlColumnName> columns ) throws CannotRetrieveDataException {
		return getIndexOf(columns, VALUE_COLUMN_NAME);
	}

	@Override
	public int highPriceIndex( final List<QuandlColumnName> columns ) throws CannotRetrieveDataException {
		return getIndexOf(columns, VALUE_COLUMN_NAME);
	}

	@Override
	public int lowPriceIndex( final List<QuandlColumnName> columns ) throws CannotRetrieveDataException {
		return getIndexOf(columns, VALUE_COLUMN_NAME);
	}

	@Override
	public int closePriceIndex( final List<QuandlColumnName> columns ) throws CannotRetrieveDataException {
		return getIndexOf(columns, VALUE_COLUMN_NAME);
	}

	private int getIndexOf( final List<QuandlColumnName> columns, final String name )
	        throws CannotRetrieveDataException {

		for (int i = 0; i < columns.size(); i++) {
			if (columnNameEquals(name, columns.get(i))) {
				return i;
			}
		}

		throw new CannotRetrieveDataException(String.format("Missing expected column: %s", name));
	}

	private boolean columnNameEquals( final String name, final QuandlColumnName column ) {
		return StringUtils.equalsIgnoreCase(name, column.getName());
	}

	private boolean containsColumnName( final String name, final List<QuandlColumnName> columns ) {
		for (int i = 0; i < columns.size(); i++) {
			if (columnNameEquals(name, columns.get(i))) {
				return true;
			}
		}

		return false;
	}
}