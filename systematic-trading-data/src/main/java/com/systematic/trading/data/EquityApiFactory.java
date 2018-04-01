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

import org.apache.commons.lang3.StringUtils;

import com.systematic.trading.configuration.exception.ConfigurationValidationException;
import com.systematic.trading.data.api.EquityApi;
import com.systematic.trading.data.api.configuration.EquityApiConfiguration;
import com.systematic.trading.data.exception.CannotRetrieveConfigurationException;
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

	public EquityApi create( final DataServiceStructure serviceType )
	        throws ConfigurationValidationException, CannotRetrieveConfigurationException {

		final EquityApiConfiguration configuration = new FileValidatedQuandlConfigurationDao().configuration();

		return new QuandlAPI(dao(serviceType, configuration), configuration, new QuandlResponseConverter());
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
