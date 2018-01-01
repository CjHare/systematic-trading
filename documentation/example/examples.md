## Examples
The example trial runs a number of backtest simulations, that are buy and hold strategies with variations of entry logic (indicator types and entry strategies)

Indicator Types:
- MACD: Moving Average Convergence Divergence
- RSI: Relative Strength Index
- EMA Uptrend: Exponential Moving Average
- SMA Uptrend: Simple Moving Average

Entry Strategies:
- Periodic: order placed every period, starting at the beginning of the simulation
- Indicator: a single configured indicator
- Periodic AND Indicator: logical AND operator, both events on same day
- Indicator AND Indicator: logical AND operator, both events on same day
- Indicator OR Indicator: logical OR operator, both events on same day
- Indicator ConfirmedBy Indicator: one indicator confirmed by another indicator, within a given time frame.

### File
- [June 2007 - June 2017: Berkshire Hathaway A](file/06_2007-06_2017-brk_a.md)
- [June 2007 - June 2017: OPEC Reference Basket (ORB)](file/06_2007-06_2017-orb.md)

### ElasticSearch
- [June 2007 - June 2017: Berkshire Hathaway A](es/06_2007-06_2017-brk_a.md)
- [June 2007 - June 2017: OPEC Reference Basket (ORB)](es/06_2007-06_2017-orb.md)
