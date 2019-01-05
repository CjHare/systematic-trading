REM NASDAQ, Berkshire Hathaway Inc, Currency USD
call analysis.bat logs/config/log4j-brk_a.xml -data_service "quandl" -data_service_structure "tables" -ticker_dataset "WIKI" -ticker_symbol "BRK-A" -opening_funds 10000
pause
exit