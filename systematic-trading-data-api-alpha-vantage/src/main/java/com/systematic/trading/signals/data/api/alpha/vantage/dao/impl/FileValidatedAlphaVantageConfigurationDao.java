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
package com.systematic.trading.signals.data.api.alpha.vantage.dao.impl;

import java.util.Properties;

import com.systematic.trading.configuration.ConfigurationValidator;
import com.systematic.trading.configuration.IntegerConfigurationValidator;
import com.systematic.trading.configuration.UrlConfigurationValidator;
import com.systematic.trading.configuration.exception.ConfigurationValidationException;
import com.systematic.trading.data.api.configuration.EquityApiConfiguration;
import com.systematic.trading.data.dao.impl.FileConfigurationDao;
import com.systematic.trading.data.exception.CannotRetrieveConfigurationException;
import com.systematic.trading.signals.data.api.alpha.vantage.configuration.AlphaVantageConfiguration;
import com.systematic.trading.signals.data.api.alpha.vantage.configuration.AlphaVantageConnectionConfiguration;
import com.systematic.trading.signals.data.api.alpha.vantage.configuration.AlphaVantageProperty;
import com.systematic.trading.signals.data.api.alpha.vantage.dao.AlphaVantageConfigurationDao;

/**
 * Implementation of QuandlConfigurationDao that validates input from file.
 * 
 * @author CJ Hare
 */
public class FileValidatedAlphaVantageConfigurationDao implements AlphaVantageConfigurationDao {

	private static final String ALPHA_VANTAGE_PROPERTIES_FILE = "alpha-vantage.properties";
	private static final String ALPHA_VANTAGE_API_KEY_FILE = "alpha-vantage.key";

	private final ConfigurationValidator<Integer> numberOfRetiresValidator;
	private final ConfigurationValidator<Integer> retryBackOffValidator;
	private final ConfigurationValidator<Integer> maximumRetrievalTimeValidator;
	private final ConfigurationValidator<Integer> maximumConcurrentConnectionValidator;
	private final ConfigurationValidator<Integer> maximumConnectionsPerSecondValidator;
	private final ConfigurationValidator<Integer> maximumMonthsPerConnectionsValidator;
	private final ConfigurationValidator<String> endpointValidator;

	public FileValidatedAlphaVantageConfigurationDao() {

		this.endpointValidator = new UrlConfigurationValidator();
		this.numberOfRetiresValidator = new IntegerConfigurationValidator(0, Integer.MAX_VALUE);
		this.retryBackOffValidator = new IntegerConfigurationValidator(0, Integer.MAX_VALUE);
		this.maximumRetrievalTimeValidator = new IntegerConfigurationValidator(500, Integer.MAX_VALUE);
		this.maximumConcurrentConnectionValidator = new IntegerConfigurationValidator(1, Integer.MAX_VALUE);
		this.maximumConnectionsPerSecondValidator = new IntegerConfigurationValidator(1, Integer.MAX_VALUE);
		this.maximumMonthsPerConnectionsValidator = new IntegerConfigurationValidator(1, Integer.MAX_VALUE);
	}

	@Override
	public EquityApiConfiguration configuration()
	        throws ConfigurationValidationException, CannotRetrieveConfigurationException {

		final String apiKey = new FileApiKeyDao().apiKey(ALPHA_VANTAGE_API_KEY_FILE);
		final Properties properties = new FileConfigurationDao().configuration(ALPHA_VANTAGE_PROPERTIES_FILE);

		final String endpoint = stringProperty(properties, AlphaVantageProperty.ENDPOINT, endpointValidator);
		final int numberOfRetries = integerProperty(
		        properties,
		        AlphaVantageProperty.NUMBER_OF_RETRIES,
		        numberOfRetiresValidator);
		final int retryBackOffMs = integerProperty(
		        properties,
		        AlphaVantageProperty.RETRY_BACKOFF_MS,
		        retryBackOffValidator);
		final int maximumRetrievalTimeSeconds = integerProperty(
		        properties,
		        AlphaVantageProperty.MAXIMUM_RETRIEVAL_TIME_SECONDS,
		        maximumRetrievalTimeValidator);
		final int maximumConcurrentConnections = integerProperty(
		        properties,
		        AlphaVantageProperty.MAXIMUM_CONCURRENT_CONNECTIONS,
		        maximumConcurrentConnectionValidator);
		final int maximumConnectionsPerSecond = integerProperty(
		        properties,
		        AlphaVantageProperty.MAXIMUM_CONNECTIONS_PER_SECOND,
		        maximumConnectionsPerSecondValidator);
		final int maximumMonthsPerConnection = integerProperty(
		        properties,
		        AlphaVantageProperty.MAXIMUM_MONTHS_RETRIEVED_PER_CONNECTION,
		        maximumMonthsPerConnectionsValidator);

		return new AlphaVantageConfiguration(
		        new AlphaVantageConnectionConfiguration(
		                endpoint,
		                apiKey,
		                numberOfRetries,
		                retryBackOffMs,
		                maximumRetrievalTimeSeconds),
		        maximumConcurrentConnections,
		        maximumConnectionsPerSecond,
		        maximumMonthsPerConnection);
	}

	private String stringProperty(
	        final Properties properties,
	        final AlphaVantageProperty property,
	        final ConfigurationValidator<String> validator ) throws ConfigurationValidationException {

		return validator.validate(property(properties, property));
	}

	private int integerProperty(
	        final Properties properties,
	        final AlphaVantageProperty property,
	        final ConfigurationValidator<Integer> validator ) throws ConfigurationValidationException {

		return validator.validate(property(properties, property));
	}

	private String property( final Properties properties, final AlphaVantageProperty property ) {

		return properties.getProperty(property.key());
	}
}
