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
package com.systematic.trading.data;

import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * Each request to retrieve the history for any ticker symbol is split up into sub requests, to
 * cater for restrictions in volume of data retrieved in one sweep.
 * 
 * @author CJ Hare
 */
@Entity
@Table(name = "history_retrieval_queue", indexes = @Index(columnList = "ticker_symbol"))
public class HistoryRetrievalRequest implements Serializable {

	/** Serialisation is required as we are using a composite primary key. */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ticker_symbol", nullable = false)
	private String tickerSymbol;

	@Id
	@Column(name = "start", nullable = false)
	private Date inclusiveStartDate;

	@Id
	@Column(name = "end", nullable = false)
	private Date exclusiveEndDate;

	public HistoryRetrievalRequest() {
		// Default constructor used by Hiberante
	}

	public HistoryRetrievalRequest( final String tickerSymbol, final LocalDate inclusiveStartDate,
	        final LocalDate exclusiveEndDate ) {
		this.tickerSymbol = tickerSymbol;
		this.inclusiveStartDate = Date.valueOf(inclusiveStartDate);
		this.exclusiveEndDate = Date.valueOf(exclusiveEndDate);
	}

	public String getTickerSymbol() {
		return tickerSymbol;
	}

	public void setTickerSymbol( final String tickerSymbol ) {
		this.tickerSymbol = tickerSymbol;
	}

	public Date getInclusiveStartDate() {
		return inclusiveStartDate;
	}

	public void setInclusiveStartDate( final Date inclusiveStartDate ) {
		this.inclusiveStartDate = inclusiveStartDate;
	}

	public Date getExclusiveEndDate() {
		return exclusiveEndDate;
	}

	public void setExclusiveEndDate( final Date exclusiveEndDate ) {
		this.exclusiveEndDate = exclusiveEndDate;
	}
}
