# How to run
Before executing the program, choices need making regarding where the price data to run the simulation comes from and where the output generated from the simulation goes.

## Price Data Source
When the requested price data is missing from the local data source, a call is made to the external price data provider. 
Valid retrieved price data is cached locally, a HyperSql database in server mode.

### Providers
- Quandl
- Yahoo Finance API (defunct)

## Event Output Type
Execution of the simulation results in a series of events that need to be aggregated and stored for later interpretation.

### Destinations
- File
- ElasticSearch

## Examples
To sample the performance and event information, there is a local cache included that contains the historical price data needed to run these [examples](example/examples.md)