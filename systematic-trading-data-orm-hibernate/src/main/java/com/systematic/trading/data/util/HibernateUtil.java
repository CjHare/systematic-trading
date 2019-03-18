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
package com.systematic.trading.data.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import com.systematic.trading.data.model.HibernateHistoryRetrievalRequest;
import com.systematic.trading.data.model.HibernateRetrievedMonthTradingPrices;

public class HibernateUtil {

	private static final Logger LOG = LogManager.getLogger(HibernateUtil.class);
	private static final SessionFactory SESSION_FACTORY = buildSessionFactory();

	private HibernateUtil() {}

	public static SessionFactory sessionFactory() {

		return SESSION_FACTORY;
	}

	private static SessionFactory buildSessionFactory() {

		try {

			final Configuration configuration = new Configuration().configure();
			configuration.configure("hibernate.cfg.xml");

			final StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
			        .applySettings(configuration.getProperties());

			configuration.addAnnotatedClass(HibernateHistoryRetrievalRequest.class);
			configuration.addAnnotatedClass(HibernateRetrievedMonthTradingPrices.class);

			final SessionFactory factory = configuration.buildSessionFactory(builder.build());

			verifyDatabaseConnection(factory);

			return factory;

		} catch (final Exception ex) {
			logCreationException(ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	private static void verifyDatabaseConnection( final SessionFactory factory ) {

		final Session session = factory.getCurrentSession();

		// When the database server is absent the beginTransaction fails
		session.beginTransaction();

		// Just in case the database is present but not 'connected'
		if (!session.isConnected()) {
			throw new ExceptionInInitializerError("Failed to connect to database");

		}

		// ...and release the resources
		session.getTransaction().commit();
	}

	private static void logCreationException( final Exception e ) {

		LOG.error("Initial SessionFactory creation failed.", e);
	}
}
