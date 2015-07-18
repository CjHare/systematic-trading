/**
 * Copyright (c) 2015, CJ Hare All rights reserved.
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
package com.systematic.trading.data.dao.impl;

import java.math.BigInteger;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.systematic.trading.data.DataPoint;
import com.systematic.trading.data.dao.DataPointDao;
import com.systematic.trading.data.util.DataPointUtil;
import com.systematic.trading.data.util.HibernateUtil;

public class DataPointDaoImpl implements DataPointDao {

	@Override
	public void create( final DataPoint[] data ) {
		final Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		final Transaction tx = session.beginTransaction();

		for (final DataPoint d : data) {
			create( d, session );
		}

		tx.commit();
	}

	@Override
	public void create( final DataPoint data ) {
		final Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		create( data, session );
		session.getTransaction().commit();
	}

	public void create( final DataPoint data, final Session session ) {
		final String sql = String
				.format(
						"INSERT INTO history_%s (date, closing_price, lowest_price, highest_price) VALUES (:date, :closing_price, :lowest_price, :highest_price)",
						sanitise( data.getTickerSymbol() ) );

		final Query query = session.createSQLQuery( sql );
		query.setDate( "date", Date.valueOf( data.getDate() ) );
		query.setBigDecimal( "closing_price", data.getClosingPrice() );
		query.setBigDecimal( "lowest_price", data.getLowestPrice() );
		query.setBigDecimal( "highest_price", data.getHighestPrice() );

		try {
			query.executeUpdate();
		} catch (final HibernateException e) {
			throw new HibernateException( String.format( "Failed inserting %s on %s", data.getTickerSymbol(),
					data.getDate() ), e );
		}
	}

	@Override
	public void createTable( final String tickerSymbol ) {
		final Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		createTable( tickerSymbol, session );
		session.getTransaction().commit();
	}

	public void createTable( final String tickerSymbol, final Session session ) {
		final StringBuilder template = new StringBuilder();
		template.append( "CREATE TABLE IF NOT EXISTS history_%s (" );
		template.append( "date DATE," );
		template.append( "closing_price DECIMAL(8,2) NOT NULL," );
		template.append( "lowest_price DECIMAL(8,2) NOT NULL," );
		template.append( "highest_price DECIMAL(8,2) NOT NULL," );
		template.append( "PRIMARY KEY (date) );" );

		final String sql = String.format( template.toString(), sanitise( tickerSymbol ) );

		final Query query = session.createSQLQuery( sql );

		query.executeUpdate();
	}

	@Override
	public DataPoint getMostRecent( final String tickerSymbol ) {
		final String sql = String.format(
				"SELECT date, closing_price, lowest_price, highest_price FROM history_%s ORDER BY date DESC LIMIT 1",
				sanitise( tickerSymbol ) );

		final Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		final Query query = session.createSQLQuery( sql );

		@SuppressWarnings("rawtypes")
		final List result = query.list();

		session.getTransaction().commit();

		if (result.isEmpty()) {
			return null;
		}

		// Convert result entries into the DataPoint
		return DataPointUtil.parseDataPoint( tickerSymbol, result.get( 0 ) );
	}

	@Override
	public DataPoint[] get( final String tickerSymbol, final LocalDate startDate, final LocalDate endDate ) {

		final String sql = String
				.format(
						"SELECT date, closing_price, lowest_price, highest_price FROM history_%s WHERE date BETWEEN :start_date AND :end_date ORDER BY date DESC",
						sanitise( tickerSymbol ) );

		final Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		final Query query = session.createSQLQuery( sql );
		query.setDate( "start_date", Date.valueOf( startDate ) );
		query.setDate( "end_date", Date.valueOf( endDate ) );

		@SuppressWarnings("rawtypes")
		final List result = query.list();

		session.getTransaction().commit();

		if (result.isEmpty()) {
			return new DataPoint[0];
		}

		// Convert result entries into the DataPoint
		final DataPoint[] data = new DataPoint[result.size()];
		for (int i = 0; i < data.length; i++) {
			data[i] = DataPointUtil.parseDataPoint( tickerSymbol, result.get( i ) );
		}

		return data;
	}

	private String sanitise( final String unsanitised ) {
		return unsanitised.replaceAll( "\\.", "_" ).replaceAll( "[-+.^:,]", "_" ).toLowerCase();
	}

	@Override
	public long count( final String tickerSymbol, final LocalDate startDate, final LocalDate endDate ) {

		final String sql = String
				.format( "SELECT count(1) FROM history_%s WHERE date BETWEEN :start_date AND :end_date",
						sanitise( tickerSymbol ) );

		final Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		final Transaction tx = session.beginTransaction();
		final Query query = session.createSQLQuery( sql );
		query.setDate( "start_date", Date.valueOf( startDate ) );
		query.setDate( "end_date", Date.valueOf( endDate ) );

		final BigInteger count = ((BigInteger) query.uniqueResult());

		tx.commit();

		return count.longValue();
	}
}
