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
package com.systematic.trading.backtest.output.elastic.resource;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.systematic.trading.backtest.output.elastic.model.ElasticFormat;
import com.systematic.trading.backtest.output.elastic.model.ElasticTypeName;
import com.systematic.trading.simulation.analysis.roi.event.ReturnOnInvestmentEvent;

/**
 * Resource for converting Equity Events to input for Elastic Search.
 * 
 * @author CJ Hare
 */
@JsonInclude(Include.NON_NULL)
public class ElasticReturnOnInvestmentEventRequestResource {

	private final float percentageChange;
	private final LocalDate inclusiveStartDate;
	private final LocalDate exclusiveEndDate;
	private final String frequency;

	public ElasticReturnOnInvestmentEventRequestResource( final ReturnOnInvestmentEvent event,
	        final String frequency ) {
		this.percentageChange = event.percentageChange().floatValue();
		this.inclusiveStartDate = event.inclusiveStartDate();
		this.exclusiveEndDate = event.exclusiveEndDate();
		this.frequency = frequency;
	}

	@JsonProperty(ElasticTypeName.PERCENTAGE_CHANGE)
	public float getPercentageChange() {

		return percentageChange;
	}

	@JsonProperty(ElasticTypeName.FREQUENCY)
	public String getFrequency() {

		return frequency;
	}

	@JsonProperty(ElasticTypeName.INCLUSIVE_START_DATE)
	@JsonFormat(pattern = ElasticFormat.LOCAL_DATE)
	public LocalDate getExlusiveStartDate() {

		return inclusiveStartDate;
	}

	@JsonProperty(ElasticTypeName.EXCLUSIVE_END_DATE)
	@JsonFormat(pattern = ElasticFormat.LOCAL_DATE)
	public LocalDate getInclusiveEndDate() {

		return exclusiveEndDate;
	}

	@Override
	public String toString() {

		final StringBuilder out = new StringBuilder("ElasticReturnOnInvestmentEventResource [");
		out.append("percentageChange=");
		out.append(percentageChange);
		out.append(", inlusiveStartDate=");
		out.append(inclusiveStartDate);
		out.append(", exclusiveEndDate=");
		out.append(exclusiveEndDate);
		out.append("]");
		return out.toString();
	}
}