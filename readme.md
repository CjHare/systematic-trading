[![Build Status](https://travis-ci.org/CjHare/systematic-trading.svg?branch=master)](https://travis-ci.org/CjHare/systematic-trading)
[![Reliability](
https://sonarcloud.io/api/badges/measure?key=com.systematic.trading:systematic-trading&metric=reliability_rating)](https://sonarcloud.io/dashboard/index/com.systematic.trading:systematic-trading)
[![Security](
https://sonarcloud.io/api/badges/measure?key=com.systematic.trading:systematic-trading&metric=security_rating)](https://sonarcloud.io/dashboard/index/com.systematic.trading:systematic-trading)
[![Maintainability](
https://sonarcloud.io/api/badges/measure?key=com.systematic.trading:systematic-trading&metric=sqale_rating)](https://sonarcloud.io/dashboard/index/com.systematic.trading:systematic-trading)

# Systematic Trading
Systematic trading refers to a way of defining goals, risk controls and rules to methodically make trading decisions.

Systematic trading covers the manual, full or partial automation of trade execution. Although systems using technical analysis are more common, there are also systems using fundamental data. Includes high frequency trading and slower types of investment such as systematic trend following and passive index tracking.

## What?
A tool to assist in understanding, evaluating and comparison of technical analysis based trading strategies.

Providing an extensible framework that retrieves historical time series price data, feeding it through a 
realistic trading simulation to generate events fed to a chosen output for later analysis. Back-testing includes configurable options, such as opening funds, periodic depositing, position sizing, entry and exit strategies using combined signals.


## Why?
Taking the broad abstraction of trading into three broad categories of triggers (trigger used for deciding entry & exit), position sizing (money management) and psychology, this project is my attempt to take a hold of the later.


### How to run

1. delete any existing database with delete-database.bat
2. start server with start-database-server.bat
3. Run all configurations over the last 10 years on NYSE:BRK_A run backtest-all-configurations.bat

* NOTE: when running from within an IDE the configuration directories need to be added to the classpath
* Building from Maven, build systematic-trading\systematic-trading-parent pom prior to the project pom


