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
package com.systematic.trading.data.util;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.systematic.trading.data.model.HistoryRetrievalRequest;

/**
 * Creation and collection operations for HistoryRetrievalRequest.
 * 
 * @author CJ Hare
 */
public class HistoryRetrievalRequestUtil {

	public List<HistoryRetrievalRequest> asList( final HistoryRetrievalRequest... requests ) {
		final List<HistoryRetrievalRequest> fulfilled = new ArrayList<HistoryRetrievalRequest>();

		for (final HistoryRetrievalRequest request : requests) {
			fulfilled.add(request);
		}

		return fulfilled;
	}

	public HistoryRetrievalRequest create( final String tickerSymbol, final LocalDate start, final LocalDate end ) {
		return new HistoryRetrievalRequest() {
			@Override
			public String getTickerSymbol() {
				return tickerSymbol;
			}

			@Override
			public Date getInclusiveStartDate() {
				return Date.valueOf(start);
			}

			@Override
			public Date getExclusiveEndDate() {
				return Date.valueOf(end);
			}
		};
	}
}