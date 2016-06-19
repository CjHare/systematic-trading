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
package com.systematic.trading.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.systematic.trading.data.util.HibernateUtil;

public class HistoryRetrievalRequestManager {

	private static final Logger LOG = LogManager.getLogger(HistoryRetrievalRequestManager.class);

	private static final HistoryRetrievalRequestManager INSTANCE = new HistoryRetrievalRequestManager();

	public static final HistoryRetrievalRequestManager getInstance() {
		return INSTANCE;
	}

	/**
	 * Persists the given set of retrieval requests.
	 * 
	 * @param requests the requests to Hibernate.
	 */
	public void create( final List<HistoryRetrievalRequest> requests ) {

		final Session session = HibernateUtil.getSessionFactory().openSession();
		for (final HistoryRetrievalRequest request : requests) {
			create(request, session);
		}
		session.close();
	}

	private void create( final HistoryRetrievalRequest request, final Session session ) {

		final Transaction tx = session.beginTransaction();

		try {
			session.save(request);
			tx.commit();
		} catch (final HibernateException e) {
			// May already have the record inserted
			LOG.info(String.format("Failed to save request for %s %s %s", request.getTickerSymbol(),
			        request.getInclusiveStartDate(), request.getExclusiveEndDate()));
			LOG.debug(e);

			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
		}

	}

	@SuppressWarnings("unchecked")
	public List<HistoryRetrievalRequest> get( final String tickerSymbol ) {

		final Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		Transaction tx = session.beginTransaction();

		try {
			final Query query = session.createQuery("from HistoryRetrievalRequest where ticker_symbol= :ticker_symbol");
			query.setString("ticker_symbol", tickerSymbol);

			return query.list();

		} catch (final HibernateException e) {
			LOG.error(e);

		} finally {
			if (tx != null) {
				tx.rollback();
			}
		}

		return new ArrayList<>(0);
	}

	public void delete( final HistoryRetrievalRequest request ) {

		final Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		Transaction tx = session.beginTransaction();

		try {
			session.delete(request);
		} catch (final HibernateException e) {
			LOG.info(String.format("Error deleting entry for %s %s %s", request.getTickerSymbol(),
			        request.getInclusiveStartDate(), request.getExclusiveEndDate()), e);
			LOG.debug(e);
		}

		tx.commit();
	}
}
