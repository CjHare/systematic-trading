@echo off
REM Build the project & start the elastic search server before running the performance trials

SET numberOfRecords=250
SET outputFile="results/all_results.csv"
SET library="target/performance-trial-elasticsearch-0.0.1-SNAPSHOT-jar-with-dependencies.jar"
SET javaOptions=-Xms1G -Xmx1G
SET classPrefix=com.systematic.trading.backtest.output.elastic.app.trial

call java %javaOptions% -cp %library% %classPrefix%.ElasticSerialSearchPerformanceTrialSingleApi %numberOfRecords% %outputFile%
call java %javaOptions% -cp %library% %classPrefix%.ElasticSearchSerialPerformanceTrialSingleApiNoReplicas %numberOfRecords% %outputFile%
call java %javaOptions% -cp %library% %classPrefix%.ElasticSearchSerialPerformanceTrialSingleApiOneShard %numberOfRecords% %outputFile%
call java %javaOptions% -cp %library% %classPrefix%.ElasticSearchParallelPerformanceTrialSingleApi %numberOfRecords% %outputFile%
call java %javaOptions% -cp %library% %classPrefix%.ElasticSearchParallelPerformanceTrialManyThreadsSingleApi %numberOfRecords% %outputFile%

exit