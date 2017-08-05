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
package com.systematic.trading.data.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.systematic.trading.data.dao.RetrievedMonthTradingPricesDao;
import com.systematic.trading.data.model.HibernateRetrievedMonthTradingPrices;
import com.systematic.trading.data.model.RetrievedMonthTradingPrices;
import com.systematic.trading.data.util.HibernateUtil;

/**
 * Hibernate implementation for the store for retrieved trading prices.
 * 
 * @author CJ Hare
 */
public class HibernateRetrievedMonthTradingPricesDao implements RetrievedMonthTradingPricesDao {

	private static final Logger LOG = LogManager.getLogger(HibernateRetrievedMonthTradingPricesDao.class);

	@Override
	public void create( final List<RetrievedMonthTradingPrices> retrieved ) {
		final Session session = HibernateUtil.getSessionFactory().openSession();
		for (final RetrievedMonthTradingPrices r : retrieved) {
			create((HibernateRetrievedMonthTradingPrices) r, session);
		}

		session.close();
	}

	@Override
	public List<RetrievedMonthTradingPrices> get( final String tickerSymbol, final int startYear, final int endYear ) {
		final Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		Transaction tx = session.beginTransaction();

		try {
			final Query query = session.createQuery(
			        "from already_retrieved_year_month where ticker_symbol= :ticker_symbol and year >= :start_year and year <= :end_year");
			query.setString("ticker_symbol", tickerSymbol);
			query.setInteger("start_year", startYear);
			query.setInteger("end_year", endYear);

			final List<?> uncast = query.list();
			return uncast.stream().map(u -> (RetrievedMonthTradingPrices) u).collect(Collectors.toList());

		} catch (final HibernateException e) {
			LOG.error(e);

		} finally {
			if (tx != null) {
				tx.rollback();
			}
		}

		return new ArrayList<>(0);
	}

	private void create( final HibernateRetrievedMonthTradingPrices request, final Session session ) {
		final Transaction tx = session.beginTransaction();

		try {
			session.save(request);
			tx.commit();
		} catch (final HibernateException e) {
			LOG.error("{}", () -> String.format("Failed to save request for %s %s", request.getTickerSymbol(),
			        request.getYearMonth()));
			LOG.error(e);

			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
		}
	}
}