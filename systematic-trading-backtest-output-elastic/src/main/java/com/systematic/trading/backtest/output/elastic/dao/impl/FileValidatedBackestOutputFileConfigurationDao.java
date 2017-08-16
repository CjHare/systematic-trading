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
package com.systematic.trading.backtest.output.elastic.dao.impl;

import java.util.Properties;

import com.systematic.trading.backtest.output.elastic.configuration.BackestOutputElasticConfiguration;
import com.systematic.trading.backtest.output.elastic.configuration.BacktestOutputElasticProperty;
import com.systematic.trading.backtest.output.elastic.configuration.impl.BackestOutputFileConfigurationImpl;
import com.systematic.trading.backtest.output.elastic.dao.BackestOutputFileConfigurationDao;
import com.systematic.trading.configuration.ConfigurationValidator;
import com.systematic.trading.configuration.IntegerConfigurationValidator;
import com.systematic.trading.data.dao.impl.FileConfigurationDao;
import com.systematic.trading.data.exception.CannotRetrieveConfigurationException;
import com.systematic.trading.exception.ConfigurationValidationException;

/**
 * Providing validation of the configuration properties for the elastic output of the back test.
 * 
 * @author CJ Hare
 */
public class FileValidatedBackestOutputFileConfigurationDao implements BackestOutputFileConfigurationDao {
	private static final String BACKTEST_OUTPUT_ELASTIC_PROPERTIES_FILE = "backtest_output_elastic.properties";

	private final ConfigurationValidator<Integer> numberOfThreadsValidator;

	public FileValidatedBackestOutputFileConfigurationDao() {
		this.numberOfThreadsValidator = new IntegerConfigurationValidator(0, Integer.MAX_VALUE);
	}

	@Override
	public BackestOutputElasticConfiguration get()
	        throws ConfigurationValidationException, CannotRetrieveConfigurationException {
		final Properties properties = new FileConfigurationDao().get(BACKTEST_OUTPUT_ELASTIC_PROPERTIES_FILE);

		final int numberOfThreads = getIntegerProperty(properties,
		        BacktestOutputElasticProperty.NUMBER_OF_THREADS, numberOfThreadsValidator);

		return new BackestOutputFileConfigurationImpl(numberOfThreads);
	}

	private int getIntegerProperty( final Properties properties, final BacktestOutputElasticProperty property,
	        final ConfigurationValidator<Integer> validator ) throws ConfigurationValidationException {
		return validator.validate(getProperty(properties, property));
	}

	private String getProperty( final Properties properties, final BacktestOutputElasticProperty property ) {
		return properties.getProperty(property.getKey());
	}
}