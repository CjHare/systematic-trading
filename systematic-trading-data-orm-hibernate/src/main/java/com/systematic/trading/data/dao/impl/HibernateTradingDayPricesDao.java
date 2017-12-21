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
package com.systematic.trading.data.dao.impl;

import java.math.BigInteger;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;

import com.systematic.trading.data.dao.TradingDayPricesDao;
import com.systematic.trading.data.util.HibernateUtil;
import com.systematic.trading.data.util.TradingDayPricesParser;
import com.systematic.trading.model.price.TradingDayPrices;

/**
 * DAO dealing with the Trading Day Prices history via Hibernate.
 * 
 * @author CJ Hare
 */
public class HibernateTradingDayPricesDao implements TradingDayPricesDao {

	private static final Logger LOG = LogManager.getLogger(HibernateTradingDayPricesDao.class);

	private final TradingDayPricesParser tradingDayPricesParser = new TradingDayPricesParser();

	@Override
	public void create( final TradingDayPrices[] data ) {

		final Session session = HibernateUtil.sessionFactory().getCurrentSession();
		final Transaction tx = session.beginTransaction();

		for (final TradingDayPrices d : data) {
			try {
				create(d, session);
			} catch (final HibernateException e) {

				if (e.getCause() instanceof ConstraintViolationException) {
					// Only constraint violation here will be primary key, or attempting to insert
					// data already present
					LOG.debug(e.getMessage());
				} else {
					throw e;
				}

			}
		}

		tx.commit();
	}

	@Override
	public void create( final TradingDayPrices data ) {

		final Session session = HibernateUtil.sessionFactory().getCurrentSession();
		session.beginTransaction();
		create(data, session);
		session.getTransaction().commit();
	}

	@Override
	public void createTableIfAbsent( final String tickerSymbol ) {

		final Session session = HibernateUtil.sessionFactory().getCurrentSession();
		session.beginTransaction();
		createTable(tickerSymbol, session);
		session.getTransaction().commit();
	}

	@Override
	public TradingDayPrices mostRecent( final String tickerSymbol ) {

		final String sql = String.format(
		        "SELECT date, opening_price, lowest_price, highest_price, closing_price FROM history_%s ORDER BY date DESC LIMIT 1",
		        sanitise(tickerSymbol));

		final Session session = HibernateUtil.sessionFactory().getCurrentSession();
		session.beginTransaction();
		final Query query = session.createSQLQuery(sql);

		@SuppressWarnings("rawtypes")
		final List result = query.list();

		session.getTransaction().commit();

		if (result.isEmpty()) {
			return null;
		}

		// Convert result entries into the DataPoint
		return tradingDayPricesParser.tradingPrices(tickerSymbol, result.get(0));
	}

	@Override
	public TradingDayPrices[] prices( final String tickerSymbol, final LocalDate startDate, final LocalDate endDate ) {

		final String sql = String.format(
		        "SELECT date, opening_price, lowest_price, highest_price, closing_price FROM history_%s WHERE date BETWEEN :start_date AND :end_date ORDER BY date DESC",
		        sanitise(tickerSymbol));

		final Session session = HibernateUtil.sessionFactory().getCurrentSession();
		session.beginTransaction();
		final Query query = session.createSQLQuery(sql);
		query.setDate("start_date", Date.valueOf(startDate));
		query.setDate("end_date", Date.valueOf(endDate));

		@SuppressWarnings("rawtypes")
		final List result = query.list();

		session.getTransaction().commit();

		if (result.isEmpty()) {
			return new TradingDayPrices[0];
		}

		// Convert result entries into the DataPoint
		final TradingDayPrices[] data = new TradingDayPrices[result.size()];
		for (int i = 0; i < data.length; i++) {
			data[i] = tradingDayPricesParser.tradingPrices(tickerSymbol, result.get(i));
		}

		return data;
	}

	@Override
	public long count( final String tickerSymbol, final LocalDate startDate, final LocalDate endDate ) {

		final String sql = String.format("SELECT count(1) FROM history_%s WHERE date BETWEEN :start_date AND :end_date",
		        sanitise(tickerSymbol));

		final Session session = HibernateUtil.sessionFactory().getCurrentSession();
		final Transaction tx = session.beginTransaction();
		final Query query = session.createSQLQuery(sql);
		query.setDate("start_date", Date.valueOf(startDate));
		query.setDate("end_date", Date.valueOf(endDate));

		final BigInteger count = (BigInteger) query.uniqueResult();

		tx.commit();

		return count.longValue();
	}

	private void create( final TradingDayPrices data, final Session session ) {

		final String sql = String.format(
		        "INSERT INTO history_%s (date, opening_price, lowest_price, highest_price, closing_price) VALUES (:date, :opening_price, :lowest_price, :highest_price, :closing_price)",
		        sanitise(data.tickerSymbol()));

		final Query query = session.createSQLQuery(sql);
		query.setDate("date", Date.valueOf(data.date()));
		query.setBigDecimal("opening_price", data.openingPrice().price());
		query.setBigDecimal("lowest_price", data.lowestPrice().price());
		query.setBigDecimal("highest_price", data.highestPrice().price());
		query.setBigDecimal("closing_price", data.closingPrice().price());

		try {
			query.executeUpdate();
		} catch (final HibernateException e) {
			throw new HibernateException(String.format("Failed inserting %s on %s", data.tickerSymbol(), data.date()),
			        e);
		}
	}

	private void createTable( final String tickerSymbol, final Session session ) {

		final StringBuilder template = new StringBuilder();
		template.append("CREATE TABLE IF NOT EXISTS history_%s (");
		template.append("date DATE,");
		template.append("opening_price DECIMAL(8,2) NOT NULL,");
		template.append("lowest_price DECIMAL(8,2) NOT NULL,");
		template.append("highest_price DECIMAL(8,2) NOT NULL,");
		template.append("closing_price DECIMAL(8,2) NOT NULL,");
		template.append("PRIMARY KEY (date) );");

		final String sql = String.format(template.toString(), sanitise(tickerSymbol));

		final Query query = session.createSQLQuery(sql);

		query.executeUpdate();
	}

	private String sanitise( final String unsanitised ) {

		return unsanitised.replaceAll("\\.", "_").replaceAll("[-+.^:,]", "_").toLowerCase();
	}
}