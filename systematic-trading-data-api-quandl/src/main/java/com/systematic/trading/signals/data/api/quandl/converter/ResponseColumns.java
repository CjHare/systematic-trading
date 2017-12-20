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
 * Provides response resource column index abstraction.
 * 
 * @author CJ Hare
 */
public interface ResponseColumns {

	/**
	 * Whether the columns are appropriate for parsing by this instance.
	 * 
	 * @param columns
	 *            the Quandl column names for the corresponding data.
	 * @return <code>true</code> when the columns can be parsed, <code>false</code> otherwise.
	 */
	boolean canParse( List<QuandlColumnName> columns );

	/**
	 * Column index for the trading day 'date' value.
	 * 
	 * @param columns
	 *            the Quandl column names for the corresponding data to find the date.
	 * @return column index for the date.
	 * @throws CannotRetrieveDataException
	 *             date column is missing from the given columns.
	 */
	int dateIndex( List<QuandlColumnName> columns ) throws CannotRetrieveDataException;

	/**
	 * Column index for the trading day 'open' price.
	 * 
	 * @param columns
	 *            the Quandl column names for the corresponding data to find the open price.
	 * @return column index for the open price.
	 * @throws CannotRetrieveDataException
	 *             open price column is missing from the given columns.
	 */
	int openPriceIndex( List<QuandlColumnName> columns ) throws CannotRetrieveDataException;

	/**
	 * Column index for the trading day 'high' price.
	 * 
	 * @param columns
	 *            the Quandl column names for the corresponding data to find the high price.
	 * @return column index for the high price.
	 * @throws CannotRetrieveDataException
	 *             high price column is missing from the given columns.
	 */
	int highPriceIndex( List<QuandlColumnName> columns ) throws CannotRetrieveDataException;

	/**
	 * Column index for the trading day 'low' price.
	 * 
	 * @param columns
	 *            the Quandl column names for the corresponding data to find the low price.
	 * @return column index for the low price.
	 * @throws CannotRetrieveDataException
	 *             low price column is missing from the given columns.
	 */
	int lowPriceIndex( List<QuandlColumnName> columns ) throws CannotRetrieveDataException;

	/**
	 * Column index for the trading day 'close' price.
	 * 
	 * @param columns
	 *            the Quandl column names for the corresponding data to find the close price.
	 * @return column index for the close price.
	 * @throws CannotRetrieveDataException
	 *             close price column is missing from the given columns.
	 */
	int closePriceIndex( List<QuandlColumnName> columns ) throws CannotRetrieveDataException;
}