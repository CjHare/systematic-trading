package com.systematic.trading.maths.model;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Test;

import com.systematic.trading.maths.DatedValue;

public class DatedValueTest {

	private final LocalDate validDate = LocalDate.now();
	private final BigDecimal validValue = BigDecimal.ZERO;

	@Test(expected = IllegalArgumentException.class)
	public void constructorDateNull() {
		new DatedValue(null, validValue);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorValueNull() {
		new DatedValue(validDate, null);
	}

	@Test
	public void getDate() {
		final DatedValue dv = new DatedValue(validDate, validValue);

		assertEquals(validDate, dv.getDate());
	}

	@Test
	public void getValue() {
		final DatedValue dv = new DatedValue(validDate, validValue);

		assertEquals(validValue, dv.getValue());
	}

	@Test
	public void getClosingPrice() {
		final DatedValue dv = new DatedValue(validDate, validValue);

		assertEquals(validValue, dv.getClosingPrice().getPrice());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getLowestPrice() {
		final DatedValue date = new DatedValue(validDate, validValue);
		date.getLowestPrice();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getHighestPrice() {
		final DatedValue date = new DatedValue(validDate, validValue);
		date.getHighestPrice();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getOpeningPrice() {
		final DatedValue date = new DatedValue(validDate, validValue);
		date.getOpeningPrice();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getTickerSymbol() {
		final DatedValue date = new DatedValue(validDate, validValue);
		date.getTickerSymbol();
	}
}
