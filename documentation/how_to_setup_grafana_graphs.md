# How to setup the Grafana graphs
After executing a simulation with ElasticSearch designated as the ouput, there's a set of Grafana graphs included in Systematic-Trading project that can be imported, but these require creating the expected data sources.


## Data Sources
Create these data sources using '+Add data srouce' from the Grafana's 'Data Source' menu

| Name                 | Type          | index Name           | Time Field Name      | Version | Group By Time Interval |
|:--------------------:|:-------------:|:--------------------:|:--------------------:|:-------:|:----------------------:|
| Brokerage            | ElasticSearch | brokerage            | transaction_date     |    5x   | 1d                     |
| Cash                 | ElasticSearch | cash                 | transaction_date     |    5x   | 1d                     |
| Equity               | ElasticSearch | equity               | transaction_date     |    5x   | 1d                     |
| Networth             | ElasticSearch | networth             | event_date           |    5x   | 1d                     |
| Order                | ElasticSearch | order                | transaction_date     |    5x   | 1d                     |
| Return On Investment | ElasticSearch | return-on-investment | inclusive_start_date |    5x   | 1d                     |
| Signal-Analysis      | ElasticSearch | signal-analysis      | signal_date          |    5x   | 1d                     |


## Graphs

###