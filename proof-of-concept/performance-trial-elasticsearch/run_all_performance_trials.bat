@echo off
SETLOCAL
REM Build the project & start the elastic search server before running the performance trials

SET numberOfRecords=250
SET outputFile=results\all_trials.csv
SET library="target/performance-trial-elasticsearch-0.0.1-SNAPSHOT-jar-with-dependencies.jar"
SET javaOptions=-Xms1G -Xmx1G
SET classPrefix=com.systematic.trading.backtest.output.elastic.app.trial

echo ----------------------------------------------
echo -------- Beginning Performance Trials --------
echo ----------------------------------------------

call :removeExistingOutputFile

REM call :runtTrial ElasticSerialSearchPerformanceTrialSingleApi
REM call :runtTrial ElasticSearchSerialPerformanceTrialSingleApiNoReplicas
REM call :runtTrial ElasticSearchSerialPerformanceTrialSingleApiOneShard
REM call :runtTrial ElasticSearchParallelPerformanceTrialSingleApi
REM call :runtTrial ElasticSearchParallelPerformanceTrialManyThreadsSingleApi
REM call :runtTrial ElasticSerialSearchPerformanceTrialSingleApiIndexRefreshDisabled
call :runtTrial ElasticSerialSearchPerformanceTrialBulkApi

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