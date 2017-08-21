REM Build the project & start the elastic search server before running the performance trials
REM SET numberOfRecords = 125
REM SET outputFile = "results/all_results.csv"
REM call java -cp "target/elasticsearch-trial-performance.jar" com.systematic.trading.backtest.output.elastic.app.trial.ElasticSearchPerformanceTrialMultiThreadedSingleApi %numberOfRecords% %outputFile%

call java -Xms1G -Xmx1G -cp "target/performance-trial-elasticsearch-0.0.1-SNAPSHOT-jar-with-dependencies.jar" com.systematic.trading.backtest.output.elastic.app.trial.ElasticSearchPerformanceTrialMultiThreadedSingleApi

exit