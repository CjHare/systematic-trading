[![Build Status](https://travis-ci.org/CjHare/systematic-trading.svg?branch=master)](https://travis-ci.org/CjHare/systematic-trading)
[![Reliability](
https://sonarcloud.io/api/badges/measure?key=com.systematic.trading:systematic-trading&metric=reliability_rating)](https://sonarcloud.io/dashboard/index/com.systematic.trading:systematic-trading)
[![Security](
https://sonarcloud.io/api/badges/measure?key=com.systematic.trading:systematic-trading&metric=security_rating)](https://sonarcloud.io/dashboard/index/com.systematic.trading:systematic-trading)
[![Maintainability](
https://sonarcloud.io/api/badges/measure?key=com.systematic.trading:systematic-trading&metric=sqale_rating)](https://sonarcloud.io/dashboard/index/com.systematic.trading:systematic-trading)

# Systematic Trading
Systematic trading refers to a way of defining goals, risk controls and rules to methodically make trading decisions.

Systematic trading can cover the manual, full or partial automation of trade execution. Although systems using technical analysis are more common, there are also some that use fundamental data. Also included are high frequency trading systems and slower types of investment such as systematic trend following and passive index tracking.

## What?
A tool to assist in understanding, evaluating and comparing technical analysis based trading strategies.

Providing an extensible framework that retrieves historical time series price data, then feeds it through a 
simulation engine to generate events, which are directed to a chosen output for later analysis. Back-testing includes configurable options, such as opening funds, periodic depositing, position sizing, entry and exit strategies using combined signals.


## Why?
Broadly speaking there are three main aspects to trading:
- Strategy: deciding entry & exit
- Position sizing: money management
- Psychology: discipline to keep to the strategy

My goal is to determine a trading system consisting of strategy and position sizing, with sufficient back-testing to also have acquired the confidence to follow the system, however events may unfold in the future.


## How to run
Before executing the program, there are some choices that need making regarding where the price data to run the simulation comes from and where the output generated from the simulation goes.

### Price Data Source
When the requested price data is missing from the local data source, a call is made to the external price data provider. 
Valid retrieved price data is cached locally, a HyperSql database in server mode.

#### Providers
- Quandl
- Yahoo Finance API (defunct)

### Event Output Type
Execution of the simulation results in a series of events that need to be aggregated and stored for later interpretation.

#### Destinations
- File
- ElasticSearch

### Examples
The local cache already contains sample data that enables running the below simulations with historical price data.

#### File
- [June 2007 - June 2017: Berkshire Hathaway A]()
- [June 2007 - June 2017: OPEC Reference Basket (ORB)]()

#### ElasticSearch
- [June 2007 - June 2017: Berkshire Hathaway A]()
- [June 2007 - June 2017: OPEC Reference Basket (ORB)]()

