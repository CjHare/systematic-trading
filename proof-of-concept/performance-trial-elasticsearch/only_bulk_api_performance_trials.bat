@echo off
SETLOCAL
REM Build the project & start the elastic search server before running the performance trials

SET numberOfRecords=3021500
SET outputFile=results\only_bulk_api_trials.csv
SET library="target/performance-trial-elasticsearch-0.0.1-SNAPSHOT-jar-with-dependencies.jar"
SET javaOptions=-Xms1G -Xmx1G
SET classPrefix=com.systematic.trading.backtest.output.elastic.app.trial

echo ----------------------------------------------
echo -------- Beginning Performance Trials --------
echo ----------------------------------------------

call :removeExistingOutputFile

call :runtTrial ElasticSearchSerialPerformanceTrialBulkApi
call :runtTrial ElasticSearchSerialPerformanceTrialBulkApiLargePayload
call :runtTrial ElasticSearchSerialPerformanceTrialBulkApiRefreshDisabled
call :runtTrial ElasticSearchSerialPerformanceTrialBulkApiMetaContainsIndex
call :runtTrial ElasticSearchSerialPerformanceTrialBulkApiMetaContainsIndexType

call :runtTrial ElasticSearchParallelPerformanceTrialBulkApi
call :runtTrial ElasticSearchParallelPerformanceTrialBulkApiManyThreads

call :runtTrial ElasticSearchSerialPerformanceTrialBulkApiOptimal

REM This test is way too slow ~10x longer then the base line ElasticSearchSerialPerformanceTrialBulkApi
REM call :runtTrial ElasticSearchSerialPerformanceTrialBulkApiTinyPayload

echo ----------------------------------------------
echo All results written to %outputFile%
echo ----------------------------------------------
echo -------- Completed Performance Trials -------- 
echo ----------------------------------------------

rem main program must end with exit /b or goto :EOF
exit /b



rem ------ SUBROUTINES ------
:removeExistingOutputFile
IF EXIST %~dp0%outputFile% (
	echo Found existing file: %~dp0%outputFile%
	del %~dp0%outputFile%
	echo Deleted 
	echo ----------------------------------------------
)
exit /b

:runtTrial
echo Begun: %1
call java %javaOptions% -cp %library% %classPrefix%.%1 %numberOfRecords% %outputFile%
echo Completed
exit /b