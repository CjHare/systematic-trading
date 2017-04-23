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
package com.systematic.trading.backtest.output.elastic.model.index.cash;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.output.elastic.BacktestBatchId;
import com.systematic.trading.backtest.output.elastic.dao.ElasticDao;
import com.systematic.trading.backtest.output.elastic.model.ElasticIndexName;
import com.systematic.trading.backtest.output.elastic.model.index.BacktestBatchIdMatcher;
import com.systematic.trading.backtest.output.elastic.model.index.JsonEntityMatcher;

@RunWith(MockitoJUnitRunner.class)
public class ElasticCashIndexTest {

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

	@Test
	public void initMissingIndex() {
		final ElasticCashIndex index = new ElasticCashIndex(dao);
		final String batchId = "BatchForTesting";
		final BacktestBatchId id = new BacktestBatchId(batchId);

		index.init(id);

		verify(dao).get(ElasticIndexName.CASH);
		verify(dao).put(eq(ElasticIndexName.CASH),
		        argThat(new JsonEntityMatcher("{\"settings\":{\"number_of_shards\":5,\"number_of_replicas\":1}}")));
		verify(dao).get(eq(ElasticIndexName.CASH), matchesBacktestId(batchId));
		verify(dao).put(eq(ElasticIndexName.CASH), matchesBacktestId(batchId), argThat(new JsonEntityMatcher(
		        "{\"entity\":{\"properties\":{\"transaction_date\":{\"type\":\"date\"},\"amount\":{\"type\":\"float\"},\"funds_after\":{\"type\":\"float\"},\"funds_before\":{\"type\":\"float\"},\"event\":{\"type\":\"text\"}}}")));

		verifyNoMoreInteractions(dao);
	}

	@Test
	public void initPresentIndexMissingMapping() {
		final ElasticCashIndex index = new ElasticCashIndex(dao);

		//TODO code
	}

	@Test
	public void initPresentIndexPresentMapping() {
		final ElasticCashIndex index = new ElasticCashIndex(dao);

		//TODO code
	}

	@Test
	public void event() {
		final ElasticCashIndex index = new ElasticCashIndex(dao);

		//TODO code		
	}

	//TODO refactor into common component
	private BacktestBatchId matchesBacktestId( final String batchId ) {
		return argThat(new BacktestBatchIdMatcher(batchId));
	}
}