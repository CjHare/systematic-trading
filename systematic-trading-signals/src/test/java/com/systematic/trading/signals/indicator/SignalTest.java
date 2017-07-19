package com.systematic.trading.signals.indicator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.systematic.trading.data.TradingDayPrices;
import com.systematic.trading.data.impl.TradingDayPricesImpl;

public abstract class SignalTest {

	protected static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;
	private static final String NO_TICKER_SYMBOL = null;

	protected TradingDayPrices[] createFlatTradingDayPrices( final int count, final int price ) {
		final TradingDayPrices[] prices = new TradingDayPrices[count];

		final LocalDate startDate = LocalDate.now().minus(count, ChronoUnit.DAYS);

		for (int i = 0; i < count; i++) {
			prices[i] = createPrice(startDate.plus(i, ChronoUnit.DAYS), BigDecimal.valueOf(price),
			        BigDecimal.valueOf(price), BigDecimal.valueOf(price), BigDecimal.valueOf(price));
		}

		return prices;
	}

	/**
	 * Steps the prices up for a period of time.
	 */
	protected TradingDayPrices[] addStep( final int start, final int count, final int increasePrice,
	        final TradingDayPrices[] prices ) {

		for (int i = start; i < start + count; i++) {
			final BigDecimal price = prices[i].getClosingPrice().getPrice().add(BigDecimal.valueOf(increasePrice));

			prices[i] = createPrice(prices[i].getDate(), price, price, price, price);
		}

		return prices;
	}

	/**
	 * Changes the values evenly at each time increment for a period of time.
	 */
	protected TradingDayPrices[] addLinearChange( final int start, final int count, final int increasePrice,
	        final TradingDayPrices[] prices ) {

		for (int i = start; i < start + count; i++) {
			final BigDecimal price = prices[i].getClosingPrice().getPrice()
			        .add(BigDecimal.valueOf((i - start) * increasePrice));

			prices[i] = createPrice(prices[i].getDate(), price, price, price, price);
		}

		return prices;
	}

	/**
	 * Keeps the BigInteger values at zero or above.
	 */
	private TradingDayPrices createPrice( final LocalDate date, final BigDecimal openingPrice,
	        final BigDecimal lowestPrice, final BigDecimal highestPrice, final BigDecimal closingPrice ) {

		final BigDecimal safeOpeningPrice = openingPrice.compareTo(BigDecimal.ZERO) > 0 ? openingPrice
		        : BigDecimal.ZERO;
		final BigDecimal safeLowestPrice = lowestPrice.compareTo(BigDecimal.ZERO) > 0 ? lowestPrice : BigDecimal.ZERO;
		final BigDecimal safeHighestPrice = highestPrice.compareTo(BigDecimal.ZERO) > 0 ? highestPrice
		        : BigDecimal.ZERO;
		final BigDecimal safeClosingPrice = closingPrice.compareTo(BigDecimal.ZERO) > 0 ? closingPrice
		        : BigDecimal.ZERO;

		return new TradingDayPricesImpl(NO_TICKER_SYMBOL, date, safeOpeningPrice, safeLowestPrice, safeHighestPrice,
		        safeClosingPrice);
	}
}