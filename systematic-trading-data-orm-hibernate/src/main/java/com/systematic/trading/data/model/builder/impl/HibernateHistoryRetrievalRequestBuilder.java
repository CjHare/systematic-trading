/**
 * Copyright (c) 2015-2018, CJ Hare All rights reserved.
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
package com.systematic.trading.data.model.builder.impl;

import java.sql.Date;
import java.time.LocalDate;

import com.systematic.trading.data.model.HibernateHistoryRetrievalRequest;
import com.systematic.trading.data.model.HistoryRetrievalRequest;
import com.systematic.trading.data.model.builder.HistoryRetrievalRequestBuilder;

/**
 * Hibernate implementation of the HistoryRetrievalRequest builder.
 * 
 * @author CJ Hare
 */
public class HibernateHistoryRetrievalRequestBuilder implements HistoryRetrievalRequestBuilder {

	private LocalDate startDateInclusive;
	private LocalDate endDateExclusive;
	private String dataset;
	private String tickerSymbol;

	@Override
	public HistoryRetrievalRequestBuilder withStartDateInclusive( Date startDateInclusive ) {

		this.startDateInclusive = startDateInclusive.toLocalDate();
		return this;
	}

	@Override
	public HistoryRetrievalRequestBuilder withEndDateExclusive( Date endDateExclusive ) {

		this.endDateExclusive = endDateExclusive.toLocalDate();
		return this;
	}

	@Override
	public HistoryRetrievalRequestBuilder withTickerSymbol( String tickerSymbol ) {

		this.tickerSymbol = tickerSymbol;
		return this;
	}

	@Override
	public HistoryRetrievalRequest build() {

		return new HibernateHistoryRetrievalRequest(dataset, tickerSymbol, startDateInclusive, endDateExclusive);
	}

	@Override
	public HistoryRetrievalRequestBuilder withEndDateExclusive( final LocalDate endDateExclusive ) {

		this.endDateExclusive = endDateExclusive;
		return this;
	}

	@Override
	public HistoryRetrievalRequestBuilder withStartDateInclusive( final LocalDate startDateInclusive ) {

		this.startDateInclusive = startDateInclusive;
		return this;
	}

	@Override
	public HistoryRetrievalRequestBuilder withDataset( final String dataset ) {

		this.dataset = dataset;
		return this;
	}
}
