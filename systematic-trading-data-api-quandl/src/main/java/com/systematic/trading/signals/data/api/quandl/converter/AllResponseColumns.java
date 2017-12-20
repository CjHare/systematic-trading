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
package com.systematic.trading.signals.data.api.quandl.converter;

import java.util.List;

import com.systematic.trading.data.exception.CannotRetrieveDataException;
import com.systematic.trading.signals.data.api.quandl.model.QuandlColumnName;

/**
 * Each of the columns expected columns are present.
 * 
 * @author CJ Hare
 */
public class AllResponseColumns implements ResponseColumns {

	private static final String DATE_COLUMN_NAME = "date";
	private static final String OPEN_PRICE_COLUMN_NAME = "open";
	private static final String HIGH_PRICE_COLUMN_NAME = "high";
	private static final String LOW_PRICE_COLUMN_NAME = "low";
	private static final String CLOSE_PRICE_COLUMN_NAME = "close";

	private final ResponseColumnsUtil columnUtils = new ResponseColumnsUtil();

	@Override
	public boolean canParse( final List<QuandlColumnName> columns ) {

		return columnUtils.containsName(DATE_COLUMN_NAME, columns)
		        && columnUtils.containsName(OPEN_PRICE_COLUMN_NAME, columns)
		        && columnUtils.containsName(HIGH_PRICE_COLUMN_NAME, columns)
		        && columnUtils.containsName(LOW_PRICE_COLUMN_NAME, columns)
		        && columnUtils.containsName(CLOSE_PRICE_COLUMN_NAME, columns);
	}

	@Override
	public int dateIndex( final List<QuandlColumnName> columns ) throws CannotRetrieveDataException {

		return columnUtils.indexOf(columns, DATE_COLUMN_NAME);
	}

	@Override
	public int openPriceIndex( final List<QuandlColumnName> columns ) throws CannotRetrieveDataException {

		return columnUtils.indexOf(columns, OPEN_PRICE_COLUMN_NAME);
	}

	@Override
	public int highPriceIndex( final List<QuandlColumnName> columns ) throws CannotRetrieveDataException {

		return columnUtils.indexOf(columns, HIGH_PRICE_COLUMN_NAME);
	}

	@Override
	public int lowPriceIndex( final List<QuandlColumnName> columns ) throws CannotRetrieveDataException {

		return columnUtils.indexOf(columns, LOW_PRICE_COLUMN_NAME);
	}

	@Override
	public int closePriceIndex( final List<QuandlColumnName> columns ) throws CannotRetrieveDataException {

		return columnUtils.indexOf(columns, CLOSE_PRICE_COLUMN_NAME);
	}
}