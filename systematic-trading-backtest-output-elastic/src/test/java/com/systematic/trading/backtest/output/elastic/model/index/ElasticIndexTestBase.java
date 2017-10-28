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
package com.systematic.trading.backtest.output.elastic.model.index;

import static com.systematic.trading.backtest.output.elastic.model.index.matcher.ElasticMatcher.equalsBacktestId;
import static com.systematic.trading.backtest.output.elastic.model.index.matcher.ElasticMatcher.equalsJson;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.BacktestBatchId;
import com.systematic.trading.backtest.output.elastic.configuration.BackestOutputElasticConfiguration;
import com.systematic.trading.backtest.output.elastic.configuration.impl.BackestOutputFileConfigurationImpl;
import com.systematic.trading.backtest.output.elastic.dao.ElasticDao;
import com.systematic.trading.backtest.output.elastic.exception.ElasticException;
import com.systematic.trading.backtest.output.elastic.model.ElasticEmptyIndexMapping;
import com.systematic.trading.backtest.output.elastic.model.ElasticIndexName;

/**
 * Purpose being to Verify the JSON messages to Elastic Search.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class ElasticIndexTestBase {

	/** Value to disable the refresh interval. */
	private static final String INDEX_SETTING_REFRESH_DISABLE = "-1";

	/** Default value for the refresh interval. */
	private static final String INDEX_SETTING_REFRESH_DEFAULT = "1s";

	@Mock
	private ElasticDao dao;

	@Mock
	private Response getIndexResponse;

	@Mock
	private Response getIndexTypeResponse;

	/** Index instance being tested. */
	private ElasticCommonIndex index;

	@Before
	public void setUp() {
		when(dao.getIndex(any(ElasticIndexName.class))).thenReturn(getIndexResponse);
		when(dao.getMapping(any(ElasticIndexName.class), any(BacktestBatchId.class))).thenReturn(getIndexTypeResponse);
	}

	@Test
	public void initMissingIndex() {
		final String batchId = "MissingIndexBatchForTesting";
		setUpIndex();

		initIndex(batchId);

		verifyMissingIndexCalls(batchId);
	}

	@Test
	public void initPresentIndexMissingMapping() {
		setUpIndexAsPresent();
		final String batchId = "MissingIndexBatchForTesting";
		setUpIndex();

		initIndex(batchId);

		verifyMissingMappingCalls(batchId);
	}

	@Test
	public void initPresentIndexPresentMapping() {
		setUpIndexAsPresent();
		setUpMappingAsPresent();
		final String batchId = "MissingIndexBatchForTesting";
		setUpIndex();

		try {
			initIndex(batchId);
			fail("Expecting an Elastic Exception");
		} catch (final ElasticException e) {
			assertEquals(
			        String.format("Existing mapping (and potentially already existing results) found for: %s", batchId),
			        e.getMessage());
		}

		verifyPresentMappingCalls(batchId);
	}

	@Test
	public void disableRefreshInterval() {
		setUpIndex();

		setRefreshInterval(false);

		verfiyRefreshInterval(false);
	}

	@Test
	public void enableRefreshInterval() {
		setUpIndex();

		setRefreshInterval(true);

		verfiyRefreshInterval(true);
	}

	@Test
	public void ensureIndexExists() {
		setUpIndex();

		createIndexIfAbsent();

		verifyPutIndexCall();
	}

	private void createIndexIfAbsent() {
		index.ensureIndexExists();
	}

	private void setRefreshInterval( final boolean refresh ) {
		index.setRefreshInterval(refresh);
	}

	private void initIndex( final String batchId ) {
		index.init(getBatchId(batchId));
	}

	private BacktestBatchId getBatchId( final String id ) {
		return new BacktestBatchId(id, null, null);
	}

	private void verifyGetIndex() {
		verify(getIndexResponse).getStatus();
		verifyNoMoreInteractions(getIndexResponse);
	}

	private void verifyGetIndexType() {
		verify(getIndexTypeResponse, atLeastOnce()).getStatus();
		verify(getIndexTypeResponse).readEntity(ElasticEmptyIndexMapping.class);
		verifyNoMoreInteractions(getIndexTypeResponse);
	}

	private void verifyPresentMappingCalls( final String batchId ) {
		verify(dao).getMapping(eq(getIndexName()), equalsBacktestId(batchId));
		verifyNoMoreInteractions(dao);

		verifyGetIndexType();
	}

	private void verifyMissingMappingCalls( final String batchId ) {
		final InOrder order = inOrder(dao);
		order.verify(dao).getMapping(eq(getIndexName()), equalsBacktestId(batchId));
		order.verify(dao).putMapping(eq(getIndexName()), equalsBacktestId(batchId),
		        equalsJson(getJsonPutIndexMapping()));
		verifyNoMoreInteractions(dao);
	}

	private void verifyPutIndexCall() {
		final InOrder order = inOrder(dao);
		order.verify(dao).getIndex(getIndexName());
		order.verify(dao).put(eq(getIndexName()), equalsJson(getJsonPutIndex()));
		verifyNoMoreInteractions(dao);

		verifyGetIndex();
	}

	private void verifyMissingIndexCalls( final String batchId ) {
		final InOrder order = inOrder(dao);
		order.verify(dao).getMapping(eq(getIndexName()), equalsBacktestId(batchId));
		order.verify(dao).putMapping(eq(getIndexName()), equalsBacktestId(batchId),
		        equalsJson(getJsonPutIndexMapping()));
		verifyNoMoreInteractions(dao);
	}

	private void setUpIndexAsPresent() {
		when(getIndexResponse.getStatus()).thenReturn(200);
	}

	private void setUpMappingAsPresent() {
		when(getIndexTypeResponse.getStatus()).thenReturn(200);
	}

	private void verfiyRefreshInterval( final boolean enabled ) {
		final String expectedJson = String.format("\"refresh_interval\":\"%s\"",
		        enabled ? INDEX_SETTING_REFRESH_DEFAULT : INDEX_SETTING_REFRESH_DISABLE);

		verify(dao).putSetting(eq(getIndexName()), equalsJson(expectedJson));
		verifyNoMoreInteractions(dao);
	}

	private void setUpIndex() {
		index = createIndex();
	}

	protected ElasticDao getDao() {
		return dao;
	}

	protected ExecutorService getPool() {
		return Executors.newSingleThreadExecutor();
	}

	protected BackestOutputElasticConfiguration getElasticConfig() {
		return new BackestOutputFileConfigurationImpl(7, 5, 1, 1);
	}

	protected abstract String getJsonPutIndex();

	protected abstract String getJsonPutIndexMapping();

	protected abstract ElasticIndexName getIndexName();

	protected abstract ElasticCommonIndex createIndex();
}