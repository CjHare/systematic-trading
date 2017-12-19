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
package com.systematic.trading.backtest.output.elastic.configuration.impl;

import com.systematic.trading.backtest.output.elastic.configuration.BackestOutputElasticConfiguration;

/**
 * Implementation for the BackestOutputFileConfiguration.
 * 
 * @author CJ Hare
 */
public class BackestOutputFileConfigurationImpl implements BackestOutputElasticConfiguration {

	private final int numberOfShards;
	private final int numberOfReplicas;
	private final int bulkApiQueueSize;
	private final int numberOfConnections;

	public BackestOutputFileConfigurationImpl( final int numberOfConnections, final int numberOfShards,
	        final int numberOfReplicas, final int bulkApiQueueSize ) {
		this.numberOfConnections = numberOfConnections;
		this.numberOfShards = numberOfShards;
		this.numberOfReplicas = numberOfReplicas;
		this.bulkApiQueueSize = bulkApiQueueSize;
	}

	@Override
	public int numberOfShards() {
		return numberOfShards;
	}

	@Override
	public int numberOfReplicas() {
		return numberOfReplicas;
	}

	@Override
	public int bulkApiQueueSize() {
		return bulkApiQueueSize;
	}

	@Override
	public int numberOfConnections() {
		return numberOfConnections;
	}
}