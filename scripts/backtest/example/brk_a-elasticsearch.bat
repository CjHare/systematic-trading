cd ..
call backtest.bat -output elastic_search -start_date 2007-06-01 -end_date 2017-06-01 -data_service_type "tables" -equity_dataset "WIKI" -ticker_symbol "BRK_A" -opening_funds 0 -deposit_amount 200 -deposit_frequency WEEKLY
cd example
exit