call java -Xms1024m -cp "systematic-trading-data-api-quandl/configuration/";"systematic-trading-backtest-output-elastic/configuration/";"systematic-trading-backtest/target/systematic-trading-backtest-0.0.1-SNAPSHOT-jar-with-dependencies.jar" com.systematic.trading.backtest.trial.AllStratgiesAgnosticSizingBrokeragetTrial -output elastic_search -start_date 2007-06-01 -end_date 2017-06-01 -ticker_symbol "BRK_A"
exit