echo off
for /f "tokens=1,* delims= " %%a in ("%*") do set ALL_BUT_FIRST=%%b

call java -Dlog4j.configurationFile=%1 -Xms1024m -cp "../../systematic-trading-data-api-quandl/configuration/";"../../systematic-trading-data-api-alpha-vantage/configuration/";"../../systematic-trading-backtest-output-file/configuration/";"../../systematic-trading-backtest-output-elastic/configuration/";"../../systematic-trading-analysis/target/systematic-trading-analysis-0.0.1-SNAPSHOT-jar-with-dependencies.jar" com.systematic.trading.analysis.EntryOrderAnalysis %ALL_BUT_FIRST%