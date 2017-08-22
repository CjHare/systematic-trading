@echo off
REM Build the project & start the elastic search server before running the performance trials

SET numberOfRecords=250
SET outputFile="results/all_results.csv"
SET library="target/performance-trial-elasticsearch-0.0.1-SNAPSHOT-jar-with-dependencies.jar"
SET javaOptions=-Xms1G -Xmx1G

call java %javaOptions% -cp %library% com.systematic.trading.backtest.output.elastic.app.trial.ElasticSearchPerformanceTrialMultiThreadedSingleApi %numberOfRecords% %outputFile%
call java %javaOptions% -cp %library% com.systematic.trading.backtest.output.elastic.app.trial.ElasticSearchPerformanceTrialSerialSingleApiOneShard %numberOfRecords% %outputFile%
call java %javaOptions% -cp %library% com.systematic.trading.backtest.output.elastic.app.trial.ElasticSearchPerformanceTrialSerialSingleApiNoReplicas %numberOfRecords% %outputFile%
call java %javaOptions% -cp %library% com.systematic.trading.backtest.output.elastic.app.trial.ElasticSearchPerformanceTrialSerialSingleApiOneShard %numberOfRecords% %outputFile%

exit