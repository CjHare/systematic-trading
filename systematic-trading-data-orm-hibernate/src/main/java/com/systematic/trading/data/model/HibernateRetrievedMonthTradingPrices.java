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
package com.systematic.trading.data.model;

import java.io.Serializable;
import java.time.YearMonth;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * Uses Hibernate ORM to store RetrievedTradingPrices.
 * 
 * @author CJ Hare
 */
@Entity
@Table(name = "already_retrieved_year_month", indexes = @Index(columnList = "ticker_symbol"))
public class HibernateRetrievedMonthTradingPrices implements Serializable, RetrievedMonthTradingPrices {

	/** Serialisation is required as we are using a composite primary key. */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ticker_symbol", nullable = false)
	private String tickerSymbol;

	@Id
	@Column(name = "month", nullable = false)
	private int month;

	@Id
	@Column(name = "year", nullable = false)
	private int year;

	public HibernateRetrievedMonthTradingPrices() {
		// Default constructor used by Hiberante
	}

	public HibernateRetrievedMonthTradingPrices( final String tickerSymbol, final YearMonth yearMonth ) {

		this.tickerSymbol = tickerSymbol;
		this.year = yearMonth.getYear();
		this.month = yearMonth.getMonthValue();
	}

	@Override
	public String tickerSymbol() {

		return tickerSymbol;
	}

	@Override
	public YearMonth yearMonth() {

		return YearMonth.of(year, month);
	}

	public void setTickerSymbol( final String tickerSymbol ) {

		this.tickerSymbol = tickerSymbol;
	}

	public void setYearMonth( final YearMonth yearMonth ) {

		this.year = yearMonth.getYear();
		this.month = yearMonth.getMonthValue();
	}

	public void setMonth( int month ) {

		this.month = month;
	}

	public void setYear( int year ) {

		this.year = year;
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder("HibernateRetrievedMonthTradingPrices [tickerSymbol=")
		        .append(tickerSymbol);
		out.append(", month=").append(month);
		out.append(", year=").append(year);
		out.append("]");
		return out.toString();
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + month;
		result = prime * result + ((tickerSymbol == null) ? 0 : tickerSymbol.hashCode());
		result = prime * result + year;
		return result;
	}

	@Override
	public boolean equals( Object obj ) {

		if (this == obj) { return true; }
		if (obj == null || getClass() != obj.getClass()) { return false; }

		final HibernateRetrievedMonthTradingPrices other = (HibernateRetrievedMonthTradingPrices) obj;

		return tickerSymbolEquals(other) && month == other.month && year == other.year;
	}

	private boolean tickerSymbolEquals( final HibernateRetrievedMonthTradingPrices other ) {

		return tickerSymbol == other.tickerSymbol || (tickerSymbol != null && tickerSymbol.equals(other.tickerSymbol));
	}
}
