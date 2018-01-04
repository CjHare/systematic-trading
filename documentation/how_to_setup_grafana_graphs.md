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
Import these provided graphs by using the any of the import options (e.g. Grafana Icon > Dashboards > Import > 'Upload .json File')

#### [Brokerage Purchase Events (Equity Value)](example/grafana_dashboards/brokerage_purchase_events_(equity_value).json)
Executed purchase orders for each strategy, by their total value (excluding fees).

#### [Brokerage Purchase Events (Equity Volume)](example/grafana_dashboards/brokerage_purchase_events_(equity_volume).json)
Executed purchase orders for each strategy, by the amount of equity acquired (excluding fees).

#### [Brokerage Transaction Fees](example/grafana_dashboards/brokerage_transaction_fees.json)
Sum of all transaction fees accrued by each strategy over the course of the simulation.

#### [Daily ROI](example/grafana_dashboards/daily_roi.json)
Daily return on investment; networth change for each strategy on every trading day.

#### [Monthly ROI](example/grafana_dashboards/monthly_roi.json)
Monthly return on investment; sum of the daily networth changes over the course of one month.

#### [Networth](example/grafana_dashboards/networth.json)
Each strategies networth (sum of cash and equity value) at the end of the simulation.

#### [Orders](example/grafana_dashboards/orders.json)
Orders events placed by the strategies, this is separate to the actual orders executed (as additional logic and variables may come into play). 

#### [Signal Events](example/grafana_dashboards/signal_events.json)
When each of the indicator used in the strategies signals events occurred. 

#### [Strategy Count](example/grafana_dashboards/strategy_count.json)
The number of strategies executed.

#### [Yearly ROI](example/grafana_dashboards/yearly_roi.json)
Annual return on investment; sum of the daily changes over the course of one month.
