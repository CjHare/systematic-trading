start start-database-server.bat
sleep 5
call java -cp systematic-trading-backtest/target/systematic-trading-backtest-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.systematic.trading.backtest.AllConfigurations
exit