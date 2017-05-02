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

import static com.systematic.trading.backtest.output.elastic.model.index.ElasticMatcher.equalsBacktestId;
import static com.systematic.trading.backtest.output.elastic.model.index.ElasticMatcher.equalsJson;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.output.elastic.BacktestBatchId;
import com.systematic.trading.backtest.output.elastic.dao.ElasticDao;
import com.systematic.trading.backtest.output.elastic.model.ElasticIndexName;

/**
 * Purpose being to Verify the JSON messages to Elastic Search.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class ElasticIndexTestBase {

	@Mock
	private ElasticDao dao;

	@Mock
	private Response getIndexResponse;

	@Mock
	private Response getIndexTypeResponse;

	@Before
	public void setUp() {
		when(dao.get(any(ElasticIndexName.class))).thenReturn(getIndexResponse);
		when(dao.get(any(ElasticIndexName.class), any(BacktestBatchId.class))).thenReturn(getIndexTypeResponse);
	}

	protected void verifyGetIndex() {
		verify(getIndexResponse).getStatus();
		verifyNoMoreInteractions(getIndexResponse);
	}

	protected void verifyGetIndexType() {
		verify(getIndexTypeResponse).getStatus();
		verifyNoMoreInteractions(getIndexTypeResponse);
	}

	protected void verifyEventCalls( final String batchId, final LocalDate transactionDate ) {
		final InOrder order = inOrder(dao);
		order.verify(dao).get(getIndexName());
		order.verify(dao).get(eq(getIndexName()), equalsBacktestId(batchId));
		order.verify(dao).put(eq(getIndexName()), equalsBacktestId(batchId), equalsJson(getJsonPutIndexMapping()));

		order.verify(dao).post(eq(getIndexName()), equalsBacktestId(batchId),
		        equalsJson(getPostIndexType(), transactionDate));
		verifyNoMoreInteractions(dao);

		verifyGetIndex();

		verify(getIndexTypeResponse).getStatus();
		verifyNoMoreInteractions(getIndexTypeResponse);
	}

	protected void verifyPresentIndexPresentMappingCalls( final String batchId ) {
		final InOrder order = inOrder(dao);
		order.verify(dao).get(getIndexName());
		order.verify(dao).get(eq(getIndexName()), equalsBacktestId(batchId));
		verifyNoMoreInteractions(dao);

		verifyGetIndex();
		verifyGetIndexType();
	}

	protected void verifyPresentIndexMissingMappingCalls( final String batchId ) {
		final InOrder order = inOrder(dao);
		order.verify(dao).get(getIndexName());
		order.verify(dao).get(eq(getIndexName()), equalsBacktestId(batchId));
		order.verify(dao).put(eq(getIndexName()), equalsBacktestId(batchId), equalsJson(getJsonPutIndexMapping()));
		verifyNoMoreInteractions(dao);

		verifyGetIndex();
	}

	protected void verifyMissingIndexCalls( final String batchId ) {
		final InOrder order = inOrder(dao);
		order.verify(dao).get(getIndexName());
		order.verify(dao).put(eq(getIndexName()), equalsJson(getJsonPutIndex()));
		order.verify(dao).get(eq(getIndexName()), equalsBacktestId(batchId));
		order.verify(dao).put(eq(getIndexName()), equalsBacktestId(batchId), equalsJson(getJsonPutIndexMapping()));
		verifyNoMoreInteractions(dao);
	}

	protected void setUpPresentIndex() {
		when(getIndexResponse.getStatus()).thenReturn(200);
	}

	protected void setUpPresentMapping() {
		when(getIndexTypeResponse.getStatus()).thenReturn(200);
	}

	protected ElasticDao getDao() {
		return dao;
	}

	protected abstract String getPostIndexType();

	protected abstract String getJsonPutIndex();

	protected abstract String getJsonPutIndexMapping();

	protected abstract ElasticIndexName getIndexName();
}