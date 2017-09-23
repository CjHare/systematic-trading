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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.systematic.trading.backtest.BacktestBatchId;
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
public class ElasticCashIndexTest extends ElasticIndexTestBase {

	private static final String JSON_PUT_INDEX = "{\"settings\":{\"number_of_shards\":5,\"number_of_replicas\":1}}";
	private static final String JSON_PUT_INDEX_MAPPING = "{\"properties\":{\"transaction_date\":{\"type\":\"date\"},\"amount\":{\"type\":\"float\"},\"funds_after\":{\"type\":\"float\"},\"funds_before\":{\"type\":\"float\"},\"event\":{\"type\":\"keyword\"}}";
	private static final String JSON_POST_INDEX_TYPE = "{\"action\":\"index\",\"type\":\"MissingIndexBatchForTesting\"},{\"event\":\"Credit\",\"transaction_date\":{\"year\":1991,\"month\":\"MAY\",\"chronology\":{\"id\":\"ISO\",\"calendarType\":\"iso8601\"},\"dayOfMonth\":29,\"dayOfWeek\":\"WEDNESDAY\",\"dayOfYear\":149,\"era\":\"CE\",\"monthValue\":5,\"leapYear\":false},\"amount\":12.34,\"funds_before\":500.12,\"funds_after\":512.46}";

	@Test
	public void initMissingIndex() {
		final String batchId = "MissingIndexBatchForTesting";
		final BacktestBatchId id = getBatchId(batchId);
		final ElasticCashIndex index = new ElasticCashIndex(getDao(), getPool(), getElasticConfig());

		index.init(id);

		verifyMissingIndexCalls(batchId);
	}

	@Test
	public void initPresentIndexMissingMapping() {
		setUpPresentIndex();

		final String batchId = "MissingIndexBatchForTesting";
		final BacktestBatchId id = getBatchId(batchId);
		final ElasticCashIndex index = new ElasticCashIndex(getDao(), getPool(), getElasticConfig());

		index.init(id);

		verifyMissingMappingCalls(batchId);
	}

	@Test
	public void initPresentIndexPresentMapping() {
		setUpPresentIndex();
		setUpPresentMapping();

		final String batchId = "MissingIndexBatchForTesting";
		final BacktestBatchId id = getBatchId(batchId);
		final ElasticCashIndex index = new ElasticCashIndex(getDao(), getPool(), getElasticConfig());

		try {
			index.init(id);
			fail("Expecting an Elastic Exception");
		} catch (final ElasticException e) {
			assertEquals(
			        String.format("Existing mapping (and potentially already existing results) found for: %s", batchId),
			        e.getMessage());
		}

		verifyPresentMappingCalls(batchId);
	}

	@Test
	public void event() {
		setUpPresentIndex();

		final String batchId = "MissingIndexBatchForTesting";
		final BacktestBatchId id = getBatchId(batchId);
		final ElasticCashIndex index = new ElasticCashIndex(getDao(), getPool(), getElasticConfig());
		final CashEvent event = getEvent();

		index.init(id);
		index.event(id, event);

		verifyEventCalls(batchId, event.getTransactionDate());
	}

	@Test
	public void disableRefreshInterval() {
		final ElasticCashIndex index = new ElasticCashIndex(getDao(), getPool(), getElasticConfig());

		index.setRefreshInterval(false);

		verfiyRefreshInterval(false);
	}

	@Test
	public void enableRefreshInterval() {
		final ElasticCashIndex index = new ElasticCashIndex(getDao(), getPool(), getElasticConfig());

		index.setRefreshInterval(true);

		verfiyRefreshInterval(true);
	}

	@Test
	public void ensureIndexExists() {
		final ElasticCashIndex index = new ElasticCashIndex(getDao(), getPool(), getElasticConfig());

		index.ensureIndexExists();

		verifyPutIndexCall();
	}

	@Override
	protected String getPostIndex() {
		return JSON_POST_INDEX_TYPE;
	}

	@Override
	protected String getJsonPutIndex() {
		return JSON_PUT_INDEX;
	}

	@Override
	protected String getJsonPutIndexMapping() {
		return JSON_PUT_INDEX_MAPPING;
	}

	@Override
	protected ElasticIndexName getIndexName() {
		return ElasticIndexName.CASH;
	}

	private CashEvent getEvent() {
		final CashEvent event = mock(CashEvent.class);
		final BigDecimal amount = BigDecimal.valueOf(12.34);
		final BigDecimal fundsAfter = BigDecimal.valueOf(512.46);
		final BigDecimal fundsBefore = BigDecimal.valueOf(500.12);
		final LocalDate transactionDate = LocalDate.of(1991, 5, 29);
		final CashEventType eventType = CashEventType.CREDIT;

		when(event.getAmount()).thenReturn(amount);
		when(event.getFundsAfter()).thenReturn(fundsAfter);
		when(event.getFundsBefore()).thenReturn(fundsBefore);
		when(event.getTransactionDate()).thenReturn(transactionDate);
		when(event.getType()).thenReturn(eventType);

		return event;
	}
}