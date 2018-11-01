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
package com.systematic.trading.data;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.systematic.trading.configuration.exception.ConfigurationValidationException;
import com.systematic.trading.data.api.EquityApi;
import com.systematic.trading.data.api.configuration.EquityApiConfiguration;
import com.systematic.trading.data.api.configuration.EquityApiLaunchArgument;
import com.systematic.trading.data.exception.CannotRetrieveConfigurationException;
import com.systematic.trading.signals.data.api.alpha.vantage.AlphaVantageAPI;
import com.systematic.trading.signals.data.api.alpha.vantage.converter.AlphaVantageResponseConverter;
import com.systematic.trading.signals.data.api.alpha.vantage.dao.impl.FileValidatedAlphaVantageConfigurationDao;
import com.systematic.trading.signals.data.api.alpha.vantage.dao.impl.HttpAlphaVantageApiDao;
import com.systematic.trading.signals.data.api.quandl.QuandlAPI;
import com.systematic.trading.signals.data.api.quandl.converter.QuandlResponseConverter;
import com.systematic.trading.signals.data.api.quandl.dao.QuandlApiDao;
import com.systematic.trading.signals.data.api.quandl.dao.impl.FileValidatedQuandlConfigurationDao;
import com.systematic.trading.signals.data.api.quandl.dao.impl.HttpQuandlDatasetApiDao;
import com.systematic.trading.signals.data.api.quandl.dao.impl.HttpQuandlDatatableApiDao;

/**
 * EquityApiFactory creates the appropriate EquityApi instance.
 * 
 * @author CJ Hare
 */
public class EquityApiFactory {

	private static final String QUANDL = "quandl";
	private static final String ALPHA_VANTAGE = "alpha-vantage";

	public Set<EquityApiLaunchArgument> launchArguments( final DataServiceType api )
	        throws ConfigurationValidationException {

		switch (api.type()) {
			case QUANDL:
				return EnumSet.of(EquityApiLaunchArgument.DATA_SERVICE_STRUCTURE);

			case ALPHA_VANTAGE:
				return EnumSet.noneOf(EquityApiLaunchArgument.class);

			// TODO different data service type for alpha vantage Crypto

			default:
				throw new ConfigurationValidationException(
				        String.format("Unsupported data service type: %s", api.type()));
		}
	}

	public EquityApi create( final DataServiceType api, final EnumMap<EquityApiLaunchArgument, ?> arguments )
	        throws ConfigurationValidationException, CannotRetrieveConfigurationException {

		switch (api.type()) {
			case QUANDL:
				return quandl(serviceStructure(arguments));

			case ALPHA_VANTAGE:
				return aplhaVantage();

			default:
				throw new ConfigurationValidationException(
				        String.format("Unsupported data service type: %s", api.type()));
		}
	}

	private DataServiceStructure serviceStructure( final EnumMap<EquityApiLaunchArgument, ?> arguments )
	        throws ConfigurationValidationException {

		if (!arguments.containsKey(
		        EquityApiLaunchArgument.DATA_SERVICE_STRUCTURE)) { throw new ConfigurationValidationException(
		                "Missing mandatory EquityApi argument of DATA_SERVICE_STRUCTURE"); }

		if (arguments.get(
		        EquityApiLaunchArgument.DATA_SERVICE_STRUCTURE) instanceof DataServiceStructure) { return (DataServiceStructure) arguments
		                .get(EquityApiLaunchArgument.DATA_SERVICE_STRUCTURE); }

		throw new ConfigurationValidationException(
		        "Missing mandatory EquityApi argument of DATA_SERVICE_STRUCTURE of type: DataServiceStructure");
	}

	private EquityApi aplhaVantage() throws ConfigurationValidationException, CannotRetrieveConfigurationException {

		final EquityApiConfiguration configuration = new FileValidatedAlphaVantageConfigurationDao().configuration();

		return new AlphaVantageAPI(
		        new HttpAlphaVantageApiDao(configuration, new AlphaVantageResponseConverter()),
		        configuration);
	}

	private EquityApi quandl( final DataServiceStructure serviceStructure )
	        throws ConfigurationValidationException, CannotRetrieveConfigurationException {

		final EquityApiConfiguration configuration = new FileValidatedQuandlConfigurationDao().configuration();

		return new QuandlAPI(dao(serviceStructure, configuration), configuration, new QuandlResponseConverter());
	}

	private QuandlApiDao dao( final DataServiceStructure type, final EquityApiConfiguration configuration ) {

		if (isTimeSeriesDataService(type)) { return new HttpQuandlDatasetApiDao(configuration); }

		if (isTablesDataService(type)) { return new HttpQuandlDatatableApiDao(configuration); }

		throw new IllegalArgumentException(String.format("Data service type not catered for: %s", type.structure()));
	}

	private boolean isTimeSeriesDataService( final DataServiceStructure type ) {

		return StringUtils.equals("time-series", type.structure());
	}

	private boolean isTablesDataService( final DataServiceStructure type ) {

		return StringUtils.equals("tables", type.structure());
	}
}
