/**
 * Copyright (c) 2015, CJ Hare
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
package com.systematic.trading.data.util;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

import com.systematic.trading.data.DataPoint;
import com.systematic.trading.data.DataPointImpl;

public class DataPointUtil {

	public static DataPoint parseDataPoint( final String tickerSymbol, final Object uncast ) {
		final Object[] data = (Object[]) uncast;
		final LocalDate date = parseDate( data[0] );
		final BigDecimal lowestPrice = parseBigDecimal( data[1] );
		final BigDecimal highestPrice = parseBigDecimal( data[2] );
		final BigDecimal closingPrice = parseBigDecimal( data[3] );

		return new DataPointImpl( tickerSymbol, date, lowestPrice, highestPrice, closingPrice );
	}

	private static LocalDate parseDate( final Object o ) {
		return Date.valueOf( o.toString() ).toLocalDate();
	}

	private static BigDecimal parseBigDecimal( final Object o ) {
		return BigDecimal.valueOf( Double.valueOf( o.toString() ) );
	}
}
