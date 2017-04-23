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

import static com.systematic.trading.backtest.output.elastic.model.index.ElasticMatcher.equalsBacktestId;
import static com.systematic.trading.backtest.output.elastic.model.index.ElasticMatcher.equalsJson;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.output.elastic.BacktestBatchId;
import com.systematic.trading.backtest.output.elastic.dao.ElasticDao;
import com.systematic.trading.backtest.output.elastic.exception.ElasticException;
import com.systematic.trading.backtest.output.elastic.model.ElasticIndexName;
import com.systematic.trading.simulation.cash.event.CashEvent;
import com.systematic.trading.simulation.cash.event.CashEvent.CashEventType;

/**
 * Purpose being to Verify the JSON messages to Elastic Search.
 * 
 * @author CJ Hare
 */
@RunWith(MockitoJUnitRunner.class)
public class ElasticCashIndexTest {

	private static final String JSON_PUT_INDEX = "{\"settings\":{\"number_of_shards\":5,\"number_of_replicas\":1}}";
	private static final String JSON_PUT_INDEX_MAPPING = "{\"entity\":{\"properties\":{\"transaction_date\":{\"type\":\"date\"},\"amount\":{\"type\":\"float\"},\"funds_after\":{\"type\":\"float\"},\"funds_before\":{\"type\":\"float\"},\"event\":{\"type\":\"text\"}}}";
	private static final String JSON_POST_INDEX_TYPE = "{\"event\":\"Credit\",\"amount\":12.34,\"fundsBefore\":500.12,\"fundsAfter\":512.46,\"transactionDate\":{\"year\":2012,\"month\":\"MARCH\",\"dayOfMonth\":14,\"dayOfWeek\":\"WEDNESDAY\"";

	//TODO common parent & merge the static classes into it too

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
		final String batchId = "MissingIndexBatchForTesting";
		final BacktestBatchId id = new BacktestBatchId(batchId);

		index.init(id);

		verify(dao).get(ElasticIndexName.CASH);
		verify(dao).put(eq(ElasticIndexName.CASH), equalsJson(JSON_PUT_INDEX));
		verify(dao).get(eq(ElasticIndexName.CASH), equalsBacktestId(batchId));
		verify(dao).put(eq(ElasticIndexName.CASH), equalsBacktestId(batchId), equalsJson(JSON_PUT_INDEX_MAPPING));
		verifyNoMoreInteractions(dao);
	}

	@Test
	public void initPresentIndexMissingMapping() {
		setUpPresentIndex();

		final ElasticCashIndex index = new ElasticCashIndex(dao);
		final String batchId = "MissingIndexBatchForTesting";
		final BacktestBatchId id = new BacktestBatchId(batchId);

		index.init(id);

		verify(dao).get(ElasticIndexName.CASH);
		verify(dao).get(eq(ElasticIndexName.CASH), equalsBacktestId(batchId));
		verify(dao).put(eq(ElasticIndexName.CASH), equalsBacktestId(batchId), equalsJson(JSON_PUT_INDEX_MAPPING));
		verifyNoMoreInteractions(dao);
		verify(getIndexResponse).getStatus();
		verifyNoMoreInteractions(getIndexResponse);
	}

	@Test
	public void initPresentIndexPresentMapping() {
		setUpPresentIndex();
		setUpPresentMapping();

		final ElasticCashIndex index = new ElasticCashIndex(dao);
		final String batchId = "MissingIndexBatchForTesting";
		final BacktestBatchId id = new BacktestBatchId(batchId);

		try {
			index.init(id);
		} catch (final ElasticException e) {
			assertEquals(
			        String.format("Existing mapping (and potentially already existing results) found for: %s", batchId),
			        e.getMessage());
		}

		verify(dao).get(ElasticIndexName.CASH);
		verify(dao).get(eq(ElasticIndexName.CASH), equalsBacktestId(batchId));
		verifyNoMoreInteractions(dao);
		verify(getIndexResponse).getStatus();
		verify(getIndexTypeResponse).getStatus();
		verifyNoMoreInteractions(getIndexResponse);
	}

	@Test
	public void event() {
		setUpPresentIndex();

		final ElasticCashIndex index = new ElasticCashIndex(dao);
		final String batchId = "MissingIndexBatchForTesting";
		final BacktestBatchId id = new BacktestBatchId(batchId);
		final CashEvent event = getEvent();

		index.init(id);
		index.event(id, event);

		//TODO inorder

		verify(dao).get(ElasticIndexName.CASH);
		verify(dao).get(eq(ElasticIndexName.CASH), equalsBacktestId(batchId));
		verify(dao).put(eq(ElasticIndexName.CASH), equalsBacktestId(batchId), equalsJson(JSON_PUT_INDEX_MAPPING));
		verify(dao).post(eq(ElasticIndexName.CASH), equalsBacktestId(batchId), equalsJson(JSON_POST_INDEX_TYPE));
		verifyNoMoreInteractions(dao);
		verify(getIndexResponse).getStatus();
		verifyNoMoreInteractions(getIndexResponse);
		verify(getIndexTypeResponse).getStatus();
		verifyNoMoreInteractions(getIndexResponse);
	}

	private void setUpPresentIndex() {
		when(getIndexResponse.getStatus()).thenReturn(200);
	}

	private void setUpPresentMapping() {
		when(getIndexTypeResponse.getStatus()).thenReturn(200);
	}

	private CashEvent getEvent() {
		final CashEvent event = mock(CashEvent.class);
		final BigDecimal amount = BigDecimal.valueOf(12.34);
		final BigDecimal fundsAfter = BigDecimal.valueOf(512.46);
		final BigDecimal fundsBefore = BigDecimal.valueOf(500.12);
		final LocalDate transactionDate = LocalDate.of(2012, 3, 14);
		final CashEventType eventType = CashEventType.CREDIT;

		when(event.getAmount()).thenReturn(amount);
		when(event.getFundsAfter()).thenReturn(fundsAfter);
		when(event.getFundsBefore()).thenReturn(fundsBefore);
		when(event.getTransactionDate()).thenReturn(transactionDate);
		when(event.getType()).thenReturn(eventType);

		return event;
	}
}