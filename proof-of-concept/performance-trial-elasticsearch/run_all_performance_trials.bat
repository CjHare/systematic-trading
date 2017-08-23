@echo off
SETLOCAL
REM Build the project & start the elastic search server before running the performance trials

SET numberOfRecords=50
SET outputFile="results/all_results.csv"
SET library="target/performance-trial-elasticsearch-0.0.1-SNAPSHOT-jar-with-dependencies.jar"
SET javaOptions=-Xms1G -Xmx1G
SET classPrefix=com.systematic.trading.backtest.output.elastic.app.trial

echo ---------------------------------------------
echo -------- Starting Performance Trials --------
echo ---------------------------------------------

call :runtTrial ElasticSerialSearchPerformanceTrialSingleApi
call :runtTrial ElasticSearchSerialPerformanceTrialSingleApiNoReplicas
call :runtTrial ElasticSearchSerialPerformanceTrialSingleApiOneShard
call :runtTrial ElasticSearchParallelPerformanceTrialSingleApi
call :runtTrial ElasticSearchParallelPerformanceTrialManyThreadsSingleApi

echo ----------------------------------------------
echo -------- Compelted Performance Trials -------- 
echo ----------------------------------------------
echo All results have been written to %outputFile%
echo ----------------------------------

rem main program must end with exit /b or goto :EOF
exit /b



rem ------ SUBROUTINES ------
:runtTrial
echo Starting: %1
call java %javaOptions% -cp %library% %classPrefix%.%1 %numberOfRecords% %outputFile%
echo Completed: %1
exit /b