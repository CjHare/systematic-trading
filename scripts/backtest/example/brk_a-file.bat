cd ..
call backtest.bat -output file_complete -start_date 2007-06-01 -end_date 2017-06-01 -data_service_type "tables" -equity_dataset "WIKI" -ticker_symbol "BRK_A" -output_file_base_directory "../../../results" -opening_funds 0 -deposit_amount 200 -deposit_frequency WEEKLY
cd example
exit