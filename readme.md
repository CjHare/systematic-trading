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


[Hwo to run](edocs/how_to_run.md)